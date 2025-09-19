package com.devin.sso.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devin.sso.common.properties.SsoProperty;
import com.devin.sso.entity.User;
import com.devin.sso.service.SamlService;
import com.devin.sso.service.UserService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameIDPolicy;
import org.opensaml.saml2.core.NameIDType;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.impl.AuthnRequestBuilder;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.NameIDPolicyBuilder;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.util.XMLHelper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * 2025/9/17 22:13.
 *
 * <p></p>
 *
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Service
//@RequiredArgsConstructor
@EnableConfigurationProperties(SsoProperty.class)
public class SamlServiceImpl implements SamlService {

    private final SsoProperty.SamlProperty samlProperty;

    private final UserService userService;

    public SamlServiceImpl(final SsoProperty ssoProperty, final UserService userService) {
        this.samlProperty = ssoProperty.getSaml();
        this.userService = userService;
    }

    @Override
    public String generateIdPLoginURL() {
        // 创建 AuthnRequest
        AuthnRequest authnRequest = createAuthRequest();

        // 2. 将 AuthnRequest 对象转换为 XML 字符串
        String authnRequestXml = marshallObject(authnRequest);

        // 3. 对 XML 字符串进行编码 (Deflate + Base64 + URL Encode)
        String encodedRequest = encodeSAMLRequest(authnRequestXml);

        return samlProperty.getIdpEntityId() + "?SAMLRequest=" + encodedRequest;
    }

    @Override
    @SneakyThrows
    public User ssoLogin(final String samlResponse) {
        BasicX509Credential credential = new BasicX509Credential();
        ClassPathResource resource = new ClassPathResource("token_jwt_key.pem");
        try (InputStream byteArrayInputStream = resource.getInputStream()) {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(byteArrayInputStream);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(certificate.getPublicKey().getEncoded());
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            credential.setPublicKey(publicKey);
        }

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(samlResponse))) {
            DefaultBootstrap.bootstrap();
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(inputStream);
            document.getDocumentElement().normalize();
            Element documentElement = document.getDocumentElement();
            UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(documentElement);
            XMLObject xmlObject = unmarshaller.unmarshall(documentElement);
            Response response = (Response) xmlObject;
            Assertion assertion = response.getAssertions().get(0);
            // 验证签名
            Signature signature = assertion.getSignature();
            if (signature == null) {
                signature = response.getSignature();
            }
            SignatureValidator signatureValidator = new SignatureValidator(credential);
            // 签名验证失败会抛错
            signatureValidator.validate(signature);

            String ssoUserId = assertion.getSubject().getNameID().getValue();
            LambdaQueryWrapper queryWrapper = new LambdaQueryWrapper<User>()
                    .eq(User::getSsoId, ssoUserId)
                    .last("limit 1");
            User user = userService.getOne(queryWrapper);
            if (user != null) {
                return user;
            }

            // 初始化用户
            user = new User();
            user.setSsoId(ssoUserId);
            user.setUsername(ssoUserId);
            userService.save(user);

            return user;
        }
    }

    /**
     * 对 XML 签名进行编码.
     * @param authnRequestXml AuthnRequest XML
     * @return  String
     */
    @SneakyThrows
    private String encodeSAMLRequest(final String authnRequestXml) {

        // 1. Deflate 压缩（防止编码之后URL请求过长）
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(baos, new Deflater(Deflater.DEFLATED, true));
        deflaterOutputStream.write(authnRequestXml.getBytes(StandardCharsets.UTF_8));
        deflaterOutputStream.close();
        byte[] deflatedBytes = baos.toByteArray();

        // 2. Base64 编码
        String base64Encoded = Base64.getEncoder().encodeToString(deflatedBytes);

        // 3. URL 编码
        return URLEncoder.encode(base64Encoded, StandardCharsets.UTF_8);
    }

    /**
     * 将 AuthnRequest 对象转换为 XML.
     * @param authnRequest AuthnRequest
     * @return  String
     */
    @SneakyThrows
    private String marshallObject(final AuthnRequest authnRequest) {
        Marshaller marshaller = org.opensaml.Configuration.getMarshallerFactory().getMarshaller(authnRequest);
        Element element = marshaller.marshall(authnRequest);

        // 使用 XMLHelper 将 DOM Element 转换为字符串
        StringWriter writer = new StringWriter();
        XMLHelper.writeNode(element, writer);
        return writer.toString();
    }

    /**
     * 创建AuthnRequest.
     * @return AuthnRequest
     */
    private AuthnRequest createAuthRequest() {
        // 创建 AuthnRequest
        AuthnRequestBuilder authnRequestBuilder = new AuthnRequestBuilder();
        AuthnRequest authnRequest = authnRequestBuilder.buildObject();

        // 设置属性值
        authnRequest.setID("_" + UUID.randomUUID());
        authnRequest.setVersion(SAMLVersion.VERSION_20);
        authnRequest.setIssueInstant(new DateTime());
        authnRequest.setDestination(samlProperty.getIdpUrl());
        authnRequest.setAssertionConsumerServiceURL(samlProperty.getAcsUrl());
        authnRequest.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);

        IssuerBuilder issuerBuilder = new IssuerBuilder();
        Issuer issuer = issuerBuilder.buildObject();
        issuer.setValue(samlProperty.getEntityId());
        authnRequest.setIssuer(issuer);

        NameIDPolicyBuilder nameIdPolicyBuilder = new NameIDPolicyBuilder();
        NameIDPolicy nameIdPolicy = nameIdPolicyBuilder.buildObject();
        nameIdPolicy.setAllowCreate(true);
        nameIdPolicy.setFormat(NameIDType.UNSPECIFIED);
        authnRequest.setNameIDPolicy(nameIdPolicy);

        return authnRequest;
    }

}
