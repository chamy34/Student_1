# Student


= Get Started with Spring Boot and SAML

One of my favorite Spring projects is Spring Security. In most cases, it simplifies web security to just a few lines of code. HTTP Basic, JDBC, JWT, OpenID Connect/OAuth 2.0, you name it&mdash;Spring Security does it!

You might notice I didn't mention SAML as an authentication type. That's because I don't recommend it. The specification for SAML 2.0 was published in March 2005, before smartphones or smart devices even existed. OpenID Connect (OIDC) is much easier for developers to use and understand. Using SAML in 2022 is like implementing a web service using WS-* instead of REST.


If you _must_ use SAML with Spring Boot, this screencast should make it quick and easy.


**Prerequisites**:

- https://adoptium.net/[Java 17]: I recommend using https://sdkman.io/[SDKMAN!] to manage and install multiple versions of Java.

* Single sign on URL: `\http://localhost:8080/login/saml2/sso/okta`
* Use this for Recipient URL and Destination URL: ✅ (the default)
* Audience URI: `\http://localhost:8080/saml2/service-provider-metadata/okta`

. Click *Next*. Select the following options:

* I'm an Okta customer adding an internal app
* This is an internal app that we have created

. Select *Finish*.

. Scroll down to the *SAML Signing Certificates* and go to *SHA-2* > *Actions* > *View IdP Metadata*. You can right-click and copy this menu item's link or open its URL. Copy the resulting link to your clipboard.

. Go to your app's *Assignment* tab and assign access to the *Everyone* group.

== Create a Spring Boot app with SAML support

. Create a Spring Boot app using https://start.spring.io[start.spring.io]. Select the following options:

* Project: *Gradle*
* Spring Boot: *3.0.6*
* Dependencies: *Spring Web*, *Spring Security*, *Thymeleaf*

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
----
====

spring:
  security:
    saml2:
      relyingparty:
        registration:
          okta:
            assertingparty:
              metadata-uri: <your-metadata-uri>
----
====

. Update `build.gradle` to add Spring Security's SAML dependency:

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
----

=== Run the app and authenticate

. Run your Spring Boot app from your IDE or using the command line:

. Open `\http://localhost:8080` in your favorite browser and log in with the credentials you used to create your account.

. If you try to log out, it won't work. Let's fix that.

=== Add a logout feature

. Edit your application on Okta and navigate to *General* > *SAML Settings* > *Edit*.

. Continue to the *Configure SAML* step and *Show Advanced Settings*. Before you can enable single logout, you'll have to create and upload a certificate to sign the outgoing logout request.

. You can create a private key and certificate using OpenSSL. Answer at least one of the questions with a value, and it should work.

openssl req -newkey rsa:2048 -nodes -keyout local.key -x509 -days 365 -out local.crt
----

. Copy the generated files to your app's `src/main/resources` directory. Configure `signing` and `singlelogout` in `application.yml`:

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

