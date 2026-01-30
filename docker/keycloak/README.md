## Spring Boot OAuth/OpenID Connect with Keycloak

## Propagate attributes from Keycloak user into the token
- Users > admin > Attributes > add key="language", value="Java"
- Client Scopes > create >
  Name = language
  > Mappers > create
  Mapper Type = User Attribute
  User Attribute = language
  Token Claim Name = language
- Clients > spring-spa > Client Scopes > add 'language' ===> the mapper of user attr is added automatically

## Client Specific roles
Are roles that only work for a given client. Can be used to restrict users from entering any application they want.


Roles composed of other roles

Create new user 'prometheus' with role METRICS for client 'spring-spa':
- User: prometheus
- Set pass: prometheus
- Role: METRICS
- Realm: Your realm (e.g., my-app)
- Token should include:

```json
{
    "realm_access": {
        "roles": ["METRICS"]
    }
}
```

## Relevant articles:
- [A Quick Guide to Using Keycloak with Spring Boot](https://www.baeldung.com/spring-boot-keycloak)
- [Custom User Attributes with Keycloak](https://www.baeldung.com/keycloak-custom-user-attributes)
- [Customizing the Login Page for Keycloak](https://www.baeldung.com/keycloak-custom-login-page)
- [Keycloak User Self-Registration](https://www.baeldung.com/keycloak-user-registration)
- [Customizing Themes for Keycloak](https://www.baeldung.com/spring-keycloak-custom-themes)
