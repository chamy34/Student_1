# Premiers pas avec Spring Boot et SAML

Si vous devez utiliser SAML avec Spring Boot, ce screencast devrait le rendre rapide et facile.

**Conditions préalables**:

- https://adoptium.net/[Java 17] : je recommande d'utiliser https://sdkman.io/[SDKMAN!] pour gérer et installer plusieurs versions de Java.

Ajouter une application SAML sur Okta
===

. Pour commencer, vous aurez besoin d'un compte développeur Okta. Vous pouvez en créer un sur https://developer.okta.com/signup[developer.okta.com/signup] ou installer https://cli.okta.com[Okta CLI] et exécuter « okta register ».

. Connectez-vous à votre compte et accédez à  **Applications** > **Create App Integration**. Sélectionnez  **SAML 2.0** et cliquez sur  **Next**. Nommez votre application comme « Spring Boot SAML » et cliquez sur  **Next**.

. Utilisez les paramètres suivants :

* Single sign on URL: `\http://localhost:8080/login/saml2/sso/okta`
* Use this for Recipient URL and Destination URL: ✅ (the default)
* Audience URI: `\http://localhost:8080/saml2/service-provider-metadata/okta`

. Cliquez sur **Next**. Sélectionnez les options suivantes:

* I'm an Okta customer adding an internal app
* This is an internal app that we have created

. Sélectionnez **Finish**.

. Scroll down to the **SAML Signing Certificates** and go to **SHA-2** > **Actions** > **View IdP Metadata**. You can right-click and copy this menu item's link or open its URL. Copy the resulting link to your clipboard.

. Go to your app's **Assignment** tab and assign access to the **Everyone** group.

Create a Spring Boot app with SAML support
===
. Create a Spring Boot app using https://start.spring.io[start.spring.io]. Select the following options:

* Project: *Gradle*
* Spring Boot: *3.0.6*
* Dependencies: *Spring Web*, *Spring Security*, *Thymeleaf*

. Add a `HomeController.java` pour renseigner les informations de l'utilisateur authentifié.
+
.`HomeController.java`
[%collapsible]

[source,java]
----
package com.example.demo;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @RequestMapping("/")
    public String home(@AuthenticationPrincipal Saml2AuthenticatedPrincipal principal, Model model) {
        model.addAttribute("name", principal.getName());
        model.addAttribute("emailAddress", principal.getFirstAttribute("email"));
        model.addAttribute("userAttributes", principal.getAttributes());
        return "home";
    }
}

----
`Home.html`
--------------
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:th="https://www.thymeleaf.org"
      xmlns:sec="https://www.thymeleaf.org/thymeleaf-extras-springsecurity6">
<head>
    <title>Spring Boot and SAML</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
</head>
<body>

<h1>Welcome</h1>
<p>You are successfully logged in as <span sec:authentication="name"></span></p>
<p>Your email address is <span th:text="${emailAddress}"></span>.</p>
<p>Your authorities are <span sec:authentication="authorities"></span>.</p>
<h2>All Your Attributes</h2>
<dl th:each="userAttribute : ${userAttributes}">
    <dt th:text="${userAttribute.key}"></dt>
    <dd th:text="${userAttribute.value}"></dd>
</dl>

<form th:action="@{/logout}" method="post">
    <button id="logout" type="submit">Logout</button>
</form>

</body>
</html>
-----------------
`application.yml`

[source,yaml]
----
spring:
  security:
    saml2:
      relyingparty:
        registration:
          okta:
            assertingparty:
              metadata-uri: <your-metadata-uri>
----

. Mettez à jour `build.gradle` pour ajouter la dépendance SAML de Spring Security :



repositories {
    ...
    maven { url "https://build.shibboleth.net/nexus/content/repositories/releases/" }
}
dependencies {
    constraints {
        implementation "org.opensaml:opensaml-core:4.1.1"
        implementation "org.opensaml:opensaml-saml-api:4.1.1"
        implementation "org.opensaml:opensaml-saml-impl:4.1.1"
    }
    ...
    implementation 'org.springframework.security:spring-security-saml2-service-provider'
}

Exécutez l'application et authentifiez-vous
==

. Ouvrez `\http://localhost:8080` dans votre navigateur préféré et connectez-vous avec les informations d'identification que vous avez utilisées pour créer votre compte.

. Si vous essayez de vous déconnecter, cela ne fonctionnera pas. Réparons ça.


Add a logout feature
==

. Modifiez votre application sur Okta et accédez à **General** > **SAML Settings** > **Edit**.

. Continue to the **Configure SAML** step and **Show Advanced Settings**. Avant de pouvoir activer la déconnexion unique, vous devrez créer et télécharger un certificat pour signer la demande de déconnexion sortante.

. Vous pouvez créer une clé privée et un certificat à l'aide d'OpenSSL. Répondez à au moins une des questions avec une valeur, et cela devrait fonctionner.

----

openssl req -newkey rsa:2048 -nodes -keyout local.key -x509 -days 365 -out local.crt

-----

. Copiez les fichiers générés dans le répertoire `src/main/resources` de votre application. Configurez `signing` et `singlelogout` dans `application.yml` :


----
spring:
  security:
    saml2:
      relyingparty:
        registration:
          okta:
            assertingparty:
              ...
            signing:
              credentials:
                - private-key-location: classpath:local.key
                  certificate-location: classpath:local.crt
            singlelogout:
              binding: POST
              response-url: "{baseUrl}/logout/saml2/slo"
----

. Upload the `local.crt` to your Okta app. Select *Enable Single Logout* and use the following values:

* Single Logout URL: `\http://localhost:8080/logout/saml2/slo`
* SP Issuer: `\http://localhost:8080/saml2/service-provider-metadata/okta`

. Finish configuring your Okta app, restart your Spring Boot app, and logout should work.

Customize authorities with Spring Security SAML
==
Vous remarquerez peut-être que lorsque vous vous connectez, la page résultante montre que vous disposez d'une autorité `ROLE_USER`. Cependant, lorsque vous avez attribué des utilisateurs à l'application, vous avez donné accès à `Everyone`. Vous pouvez configurer votre application SAML sur Okta pour envoyer les groupes d'utilisateurs en tant qu'attribut. Vous pouvez également ajouter d'autres attributs tels que le nom et l'adresse e-mail.

. Modifiez les paramètres SAML de votre application Okta et remplissez la section **Group Attribute Statements**.

* Name: `groups`
* Name format: `Unspecified`
* Filter: `Matches regex` and use `.*` for the value

.Juste au-dessus, vous pouvez ajouter d'autres instructions d'attribut. Par exemple:
+
|===
|Name |Name format|Value

|`email`
|`Unspecified`
|`user.email`

|`firstName`
|`Unspecified`
|`user.firstName`

|`lastName`
|`Unspecified`
|`user.lastName`
|===

. *Save* these changes.

. Create a `SecurityConfiguration` class that overrides the default configuration and uses a converter to translate the values in the `groups` attribute into Spring Security authorities. [`saml-security-config`]


----
package com.example.demo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider.ResponseToken;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain configure(HttpSecurity http) throws Exception {

        OpenSaml4AuthenticationProvider authenticationProvider = new OpenSaml4AuthenticationProvider();
        authenticationProvider.setResponseAuthenticationConverter(groupsConverter());

        http.authorizeHttpRequests(authorize -> authorize
                .anyRequest().authenticated())
            .saml2Login(saml2 -> saml2
                .authenticationManager(new ProviderManager(authenticationProvider)))
            .saml2Logout(withDefaults());

        return http.build();
    }

    private Converter<OpenSaml4AuthenticationProvider.ResponseToken, Saml2Authentication> groupsConverter() {

        Converter<ResponseToken, Saml2Authentication> delegate =
            OpenSaml4AuthenticationProvider.createDefaultResponseAuthenticationConverter();

        return (responseToken) -> {
            Saml2Authentication authentication = delegate.convert(responseToken);
            Saml2AuthenticatedPrincipal principal = (Saml2AuthenticatedPrincipal) authentication.getPrincipal();
            List<String> groups = principal.getAttribute("groups");
            Set<GrantedAuthority> authorities = new HashSet<>();
            if (groups != null) {
                groups.stream().map(SimpleGrantedAuthority::new).forEach(authorities::add);
            } else {
                authorities.addAll(authentication.getAuthorities());
            }
            return new Saml2Authentication(principal, authentication.getSaml2Response(), authorities);
        };
    }
}
----
====

. Restart your app and log in, you should see your user's groups as authorities.


