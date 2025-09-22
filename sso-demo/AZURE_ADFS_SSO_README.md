# Azure ADFS 单点登录集成指南

这个文档说明如何使用已实现的 Azure ADFS 单点登录功能，支持两种协议：**OIDC** 和 **纯 OAuth2**。

## OAuth2 vs OIDC 的区别

### 核心差异

| 特性 | OAuth2 | OIDC |
|------|--------|------|
| **主要目的** | 授权 (Authorization) | 身份认证 (Authentication) |
| **Scope 要求** | 任意自定义 scope | **必须包含 `openid`** |
| **返回的 Token** | `access_token` + `refresh_token` | `access_token` + **`id_token`** + `refresh_token` |
| **用户信息获取** | 用 `access_token` 调用 `/userinfo` 端点 | 直接从 `id_token` 解析 |
| **Token 格式** | 不要求特定格式 | `id_token` 必须是 JWT |

### 流程对比

**OIDC 流程**：
1. 授权请求包含 `openid` scope
2. 返回 `access_token` + `id_token`
3. 直接验证和解析 `id_token` 获取用户信息

**纯 OAuth2 流程**：
1. 授权请求不包含 `openid` scope
2. 只返回 `access_token`
3. 用 `access_token` 调用 `/userinfo` 端点获取用户信息

## 功能特性

- ✅ **支持两种协议**：OIDC 和纯 OAuth2
- ✅ 基于 OAuth2 Authorization Code Flow 的 Azure ADFS 集成
- ✅ 自动端点发现 (.well-known/openid-configuration)
- ✅ JWT ID Token 验证和解析（OIDC）
- ✅ Access Token + UserInfo 端点调用（纯 OAuth2）
- ✅ 用户自动创建和登录
- ✅ 不依赖 Spring Security 框架
- ✅ 参考现有 OIDC 实现模式

## 配置步骤

### 1. Azure ADFS 配置

在 Azure ADFS 中注册应用程序：
1. 在 ADFS 管理控制台中创建新的应用程序组
2. 添加 Web 应用程序，配置重定向 URI: `http://localhost:8080/sso/azure-adfs/callback`
3. 记录应用程序的 Client ID 和 Client Secret

### 2. 应用程序配置

更新 `src/main/resources/application.yaml` 中的 Azure ADFS 配置：

```yaml
sso:
  azure-adfs:
    # 替换为你的实际配置
    tenant-id: your-actual-tenant-id
    client-id: your-actual-client-id
    client-secret: your-actual-client-secret
    redirect-uri: http://localhost:8080/sso/azure-adfs/callback
    scope: openid profile email
    authority: https://your-adfs-server/adfs
    home-url: http://localhost:8080
```

## API 端点

### OIDC 端点（推荐用于身份认证）

#### 1. OIDC 登录
```
GET /sso/azure-adfs/login
```
重定向用户到 Azure ADFS 登录页面（OIDC 流程，包含 `openid` scope）。

#### 2. OIDC 回调
```
GET /sso/azure-adfs/callback?code={code}&state={state}
```
处理 Azure ADFS 的登录回调，使用 ID Token 完成用户认证。

### 纯 OAuth2 端点（用于对比学习）

#### 3. 纯 OAuth2 登录
```
GET /sso/oauth2-pure/login
```
重定向用户到 Azure ADFS 登录页面（纯 OAuth2 流程，不包含 `openid` scope）。

#### 4. 纯 OAuth2 回调
```
GET /sso/oauth2-pure/callback?code={code}&state={state}
```
处理 Azure ADFS 的登录回调，使用 Access Token 调用 UserInfo 端点获取用户信息。

### 工具端点

#### 5. 获取配置信息
```
GET /sso/azure-adfs/config
```
返回当前的 Azure ADFS 配置信息（调试用）。

## 使用流程

### OIDC 流程（推荐）

1. **发起登录**: 用户访问 `/sso/azure-adfs/login`
2. **Azure ADFS 认证**: 系统重定向到 Azure ADFS 登录页面（包含 `openid` scope）
3. **用户登录**: 用户在 Azure ADFS 页面输入凭证
4. **回调处理**: Azure ADFS 重定向回 `/sso/azure-adfs/callback` 并携带授权码
5. **令牌交换**: 系统使用授权码交换 `access_token` 和 `id_token`
6. **ID Token 验证**: 验证 ID Token 的签名、发行者、受众等
7. **用户信息解析**: 直接从 ID Token 中解析用户信息
8. **用户登录**: 创建或更新本地用户记录
9. **登录完成**: 重定向到主页

### 纯 OAuth2 流程（对比学习）

1. **发起登录**: 用户访问 `/sso/oauth2-pure/login`
2. **Azure ADFS 认证**: 系统重定向到 Azure ADFS 登录页面（不包含 `openid` scope）
3. **用户登录**: 用户在 Azure ADFS 页面输入凭证
4. **回调处理**: Azure ADFS 重定向回 `/sso/oauth2-pure/callback` 并携带授权码
5. **令牌交换**: 系统使用授权码交换 `access_token`（没有 `id_token`）
6. **调用用户信息端点**: 使用 `access_token` 调用 `/userinfo` 端点
7. **用户信息获取**: 从 UserInfo 端点响应中获取用户信息
8. **用户登录**: 创建或更新本地用户记录
9. **登录完成**: 重定向到主页

## 核心类说明

### SsoServiceImpl
主要的业务逻辑实现类，包含：

**OIDC 相关方法**：
- `generateAzureAdfsLoginUrl()`: 生成 OIDC 登录 URL（包含 `openid` scope）
- `exchangeCodeForTokensAndVerify()`: OIDC 令牌交换和 ID Token 验证
- `verifyIdToken()`: JWT ID Token 验证
- `login()`: 用户登录处理

**纯 OAuth2 相关方法**：
- `generateOAuth2LoginUrl()`: 生成纯 OAuth2 登录 URL（不包含 `openid` scope）
- `exchangeCodeForTokensOAuth2Style()`: 纯 OAuth2 令牌交换
- `getUserInfoWithAccessToken()`: 使用 Access Token 调用 UserInfo 端点

**通用方法**：
- `initializeAzureAdfsEndpoints()`: 端点自动发现

### SsoController
REST API 控制器，包含：

**OIDC 端点**：
- `/azure-adfs/login`: OIDC 登录端点
- `/azure-adfs/callback`: OIDC 回调端点

**纯 OAuth2 端点**：
- `/oauth2-pure/login`: 纯 OAuth2 登录端点
- `/oauth2-pure/callback`: 纯 OAuth2 回调端点

**工具端点**：
- `/azure-adfs/config`: 配置信息端点

### AzureAdfsProperty
配置属性类，定义所有必要的 Azure ADFS 配置参数。

## 安全考虑

1. **State 参数**: 实现了 state 参数生成，防止 CSRF 攻击
2. **Token 验证**: 严格验证 ID Token 的签名、发行者和受众
3. **HTTPS**: 生产环境必须使用 HTTPS
4. **密钥管理**: 客户端密钥应妥善保管，不要提交到版本控制

## 故障排除

### 常见问题

1. **端点发现失败**
   - 检查 `authority` URL 是否正确
   - 确认 ADFS 服务器支持 OpenID Connect 发现

2. **令牌交换失败**
   - 验证 client_id 和 client_secret 是否正确
   - 检查 redirect_uri 是否与 ADFS 中配置的一致

3. **Token 验证失败**
   - 确认 JWK Set URI 可访问
   - 检查系统时间是否正确

### 调试模式

启用调试日志以获取更多信息：
```yaml
logging:
  level:
    com.devin.sso: DEBUG
```

## 扩展建议

1. **会话管理**: 集成现有的会话管理机制（如 Shiro）
2. **用户映射**: 根据业务需求映射更多用户属性
3. **权限管理**: 基于 Azure ADFS 组信息设置用户权限
4. **缓存优化**: 缓存 JWK Set 以提高性能

## 参考资料

- [OAuth 2.0 Authorization Code Flow](https://tools.ietf.org/html/rfc6749#section-4.1)
- [OpenID Connect Core 1.0](https://openid.net/specs/openid-connect-core-1_0.html)
- [Azure ADFS OAuth2 文档](https://docs.microsoft.com/en-us/windows-server/identity/ad-fs/)
