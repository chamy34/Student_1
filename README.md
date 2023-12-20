# Premiers pas avec Spring Boot et SAML

Si vous devez utiliser SAML avec Spring Boot, ce screencast devrait le rendre rapide et facile.

**Conditions préalables**:

- https://adoptium.net/[Java 17] : je recommande d'utiliser https://sdkman.io/[SDKMAN!] pour gérer et installer plusieurs versions de Java.

== Ajouter une application SAML sur Okta
=====

. Pour commencer, vous aurez besoin d'un compte développeur Okta. Vous pouvez en créer un sur https://developer.okta.com/signup[developer.okta.com/signup] ou installer https://cli.okta.com[Okta CLI] et exécuter « okta register ».

. Connectez-vous à votre compte et accédez à *Applications* > *Create App Integration*. Sélectionnez *SAML 2.0* et cliquez sur *Next*. Nommez votre application comme « Spring Boot SAML » et cliquez sur *Next*.

. Utilisez les paramètres suivants :

* Single sign on URL: `\http://localhost:8080/login/saml2/sso/okta`
* Use this for Recipient URL and Destination URL: ✅ (the default)
* Audience URI: `\http://localhost:8080/saml2/service-provider-metadata/okta`

. Cliquez sur *Next*. Sélectionnez les options suivantes:

* I'm an Okta customer adding an internal app
* This is an internal app that we have created

. Sélectionnez *Finish*.
