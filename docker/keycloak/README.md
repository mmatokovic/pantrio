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

## Hierarchical roles
Roles composed of other roles

## Client Specific roles
Are roles that only work for a given client. Can be used to restrict users from entering any application they want.

## Relevant articles:
- [A Quick Guide to Using Keycloak with Spring Boot](https://www.baeldung.com/spring-boot-keycloak)
- [Custom User Attributes with Keycloak](https://www.baeldung.com/keycloak-custom-user-attributes)
- [Customizing the Login Page for Keycloak](https://www.baeldung.com/keycloak-custom-login-page)
- [Keycloak User Self-Registration](https://www.baeldung.com/keycloak-user-registration)
- [Customizing Themes for Keycloak](https://www.baeldung.com/spring-keycloak-custom-themes)
