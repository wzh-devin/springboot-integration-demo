package com.devin.sso.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devin.sso.common.properties.SsoProperty;
import com.devin.sso.entity.User;
import com.devin.sso.service.SamlService;
import com.devin.sso.service.UserService;
import com.nimbusds.jose.shaded.gson.JsonObject;
import io.swagger.v3.core.util.Json;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
public class SamlServiceImpl implements SamlService {

    private SsoProperty.SamlProperty samlProperty;

    private static final String samlConfig = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress";

    private final UserService userService;

    public SamlServiceImpl(final UserService userService) {
//        this.samlProperty = ssoProperty.getSaml();
        this.userService = userService;
    }

    @Override
    public String generateIdPLoginURL() {
        // 创建 AuthnRequest
//        AuthnRequest authnRequest = createAuthRequest();
//
//        // 2. 将 AuthnRequest 对象转换为 XML 字符串
//        String authnRequestXml = marshallObject(authnRequest);
//
//        // 3. 对 XML 字符串进行编码 (Deflate + Base64 + URL Encode)
//        String encodedRequest = encodeSAMLRequest(authnRequestXml);

        return "https://galaxy.capitaland.com/adfs/ls/" + "?SAMLRequest=" + "nZLNbtswEIRfReBdokTFaULYBtwYRQykrRA7OeRSrMi1Q4AiWS6VOm9fWc5vgeTQGzGYwXyY5ZSgs0Eu%2BnTvrvF3j5Sy1XLGfrWi1oBtlVd1Xecnp1Wbn7Va5eXktNVnQp8LUbHsFiMZ72ZMFCXLVkQ9rhwlcGmQSjHJy%2FNciE1VyuqLnJwUoqzvWLYcWoyDNCbvUwokOd%2BBhf1joSCYNDydLpTvOOgtcUucZd98VDhyztgWLOGhrwEi84AvShN98srbr8Zp43Yz1kcnPZAh6aBDkknJ9eL7lRx4ZXs0kbzcbJq8%2BbnesGxBhPEAduEd9R3GNcYHo%2FDm%2BuoVNRpC6iH9A1sox1UHHEKwfmdcART2LNt31pEcd%2F6cJzzBs%2Fn04JbjnPFN%2FvM4PKOz%2Bf%2BATvmb0iNBkD%2BGltWy8daox8MFOkgfQ1RFNSpG59vRKntHAZXZGtTDtNb6PxcRIQ33SrEfzsXnx9b3H3D%2BFw%3D%3D";
    }

    @Override
    @SneakyThrows
    public User ssoLogin(final String samlResponse) {
        BasicX509Credential credential = new BasicX509Credential();
//        ClassPathResource resource = new ClassPathResource("token_jwt_key.pem");
        // "MIIC5jCCAc6gAwIBAgIQeVg4yKGv24FOFG633m2YuTANBgkqhkiG9w0BAQsFADAvMS0wKwYDVQQDEyRBREZTIFNpZ25pbmcgLSBnYWxheHkuY2FwaXRhbGFuZC5jb20wHhcNMjEwMTIwMTMzNzMzWhcNMzEwMTE4MTMzNzMzWjAvMS0wKwYDVQQDEyRBREZTIFNpZ25pbmcgLSBnYWxheHkuY2FwaXRhbGFuZC5jb20wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDgddfKRjScmfbzaSwA9WUqqZjN0eRwyALol3xsPLFWELEZ0OJl9tNNCV0WqpacbJ6RBVtk44FYQcaMUly7nRqA8ymuLnwylQe4zUmICCrm3mVvnuQ6GoJayjTAoXNjijQiKXDFcHfyNQIpbq5ms1mKSZHWhC36JVtIh4rdAfpI/De26CTwYBzr3ebjBhvX7lRrT7k14I3pLBSR8kEpkxN7flxI6EQCTi7SnpbALVdId4OqAHqZbVedH0wCPKVNFh6Re4ZhBVrNaEkoWIPC+WbCPGGxn89hWRa27xrVC+zboapyTwg9xwqd10jWBZH6ZsEUDpP6KrVPX4Z322MN+qGTAgMBAAEwDQYJKoZIhvcNAQELBQADggEBAM7q61H0n7NOAYHvtSQh6Xmyrz151u/MZeU79eWU9aHzmz8nG+qCCv6J2BknB6wY6Ir2hQ9XMSTOHUmf2nTTdJfuySfqWuf1KQkHQen8Lm/JXeyEYuxo4LjbTrtCtY0P3iL5JgjOkxphWceTpjCVT8oo85S+ZoVeqGg6Gb9qAvHDfiaKIkAEAp0HpKhp2tVPaFjdGfumdBZknWvsc37UJIpQp+Vh+2nojmeJdwz7XVEQgXPj3r1935S2IiuXOQxJ5txFB8IEskth4X3YsFzoPW6130we5ttKd3Br+0gwQ8oZD8B8/ZQ/dbREnHQpeEfDocMRjy3GKxHiE87jKounDoc="
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Base64.getDecoder().decode("MIIC5jCCAc6gAwIBAgIQeVg4yKGv24FOFG633m2YuTANBgkqhkiG9w0BAQsFADAvMS0wKwYDVQQDEyRBREZTIFNpZ25pbmcgLSBnYWxheHkuY2FwaXRhbGFuZC5jb20wHhcNMjEwMTIwMTMzNzMzWhcNMzEwMTE4MTMzNzMzWjAvMS0wKwYDVQQDEyRBREZTIFNpZ25pbmcgLSBnYWxheHkuY2FwaXRhbGFuZC5jb20wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDgddfKRjScmfbzaSwA9WUqqZjN0eRwyALol3xsPLFWELEZ0OJl9tNNCV0WqpacbJ6RBVtk44FYQcaMUly7nRqA8ymuLnwylQe4zUmICCrm3mVvnuQ6GoJayjTAoXNjijQiKXDFcHfyNQIpbq5ms1mKSZHWhC36JVtIh4rdAfpI/De26CTwYBzr3ebjBhvX7lRrT7k14I3pLBSR8kEpkxN7flxI6EQCTi7SnpbALVdId4OqAHqZbVedH0wCPKVNFh6Re4ZhBVrNaEkoWIPC+WbCPGGxn89hWRa27xrVC+zboapyTwg9xwqd10jWBZH6ZsEUDpP6KrVPX4Z322MN+qGTAgMBAAEwDQYJKoZIhvcNAQELBQADggEBAM7q61H0n7NOAYHvtSQh6Xmyrz151u/MZeU79eWU9aHzmz8nG+qCCv6J2BknB6wY6Ir2hQ9XMSTOHUmf2nTTdJfuySfqWuf1KQkHQen8Lm/JXeyEYuxo4LjbTrtCtY0P3iL5JgjOkxphWceTpjCVT8oo85S+ZoVeqGg6Gb9qAvHDfiaKIkAEAp0HpKhp2tVPaFjdGfumdBZknWvsc37UJIpQp+Vh+2nojmeJdwz7XVEQgXPj3r1935S2IiuXOQxJ5txFB8IEskth4X3YsFzoPW6130we5ttKd3Br+0gwQ8oZD8B8/ZQ/dbREnHQpeEfDocMRjy3GKxHiE87jKounDoc="))) {
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
//            String ssoUserId = assertion.getSubject().getNameID().getValue();

            String attributeName = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress";

            String ssoUserId = getSsoUserId(assertion);

            try {
                Marshaller marshaller = Configuration.getMarshallerFactory().getMarshaller(assertion);
                Element element = marshaller.marshall(assertion);
                StringWriter writer = new StringWriter();
                XMLHelper.writeNode(element, writer);
                String assertionXml = writer.toString();
                log.info("Assertion XML:\n{}", assertionXml);
            } catch (Exception e) {
                log.error("Failed to serialize assertion to XML", e);
            }

//            LambdaQueryWrapper queryWrapper = new LambdaQueryWrapper<User>()
//                    .eq(User::getSsoId, ssoUserId)
//                    .last("limit 1");
//            User user = userService.getOne(queryWrapper);
//            if (user != null) {
//                return user;
//            }
//
//            // 初始化用户
//            user = new User();
//            user.setSsoId(ssoUserId);
//            user.setUsername(ssoUserId);
//            userService.save(user);

            return new User();
        }
    }

    /**
     * 对 XML 签名进行编码.
     *
     * @param authnRequestXml AuthnRequest XML
     * @return String
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
     *
     * @param authnRequest AuthnRequest
     * @return String
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
     *
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

    /**
     * 获取SSO用户唯一ID.
     *
     * @param assertion SAML assertion
     * @return ssoUserId
     */
    private String getSsoUserId(final Assertion assertion) {
        NameID nameID = assertion.getSubject().getNameID();
        // 判断nameID是否为空
        if (Objects.nonNull(nameID)) {
            return nameID.getValue();
        }
        // 从配置中AttributeStatement中获取唯一标识
        checkAttributeName();
        // 获取属性值的xml列表
        XMLObject xmlObject = Optional.ofNullable(assertion.getAttributeStatements())
                .stream()
                .flatMap(Collection::stream)
                .map(AttributeStatement::getAttributes)
                .flatMap(Collection::stream)
                .filter(attribute -> samlConfig.equals(attribute.getName()))
                .map(Attribute::getAttributeValues)
                .flatMap(Collection::stream)
                .findFirst()
                // 如果没有数据，则抛出异常信息
                .orElseThrow(() -> new RuntimeException("sssss"));

        return xmlObject.getDOM().getTextContent();
    }

    /**
     * 检查属性名称是否配置.
     */
    private void checkAttributeName() {
        String attributeName = samlConfig;
    }

}
