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
+
You can also use https://start.spring.io/#!type=gradle-project&language=java&platformVersion=3.0.6&packaging=jar&jvmVersion=17&groupId=com.example&artifactId=demo&name=demo&description=Demo%20project%20for%20Spring%20Boot&packageName=com.example.demo&dependencies=web,security,thymeleaf[this URL] or HTTPie:
+
[source,shell]
----
https start.spring.io/starter.zip bootVersion==3.0.6 \
  dependencies==web,security,thymeleaf type==gradle-project \
  baseDir==spring-boot-saml | tar -xzvf -
----
