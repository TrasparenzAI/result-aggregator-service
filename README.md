# Transparency Result Aggregator Service
## Transparency Result Aggregator - REST Services

[![Supported JVM Versions](https://img.shields.io/badge/JVM-21-brightgreen.svg?style=for-the-badge&logo=Java)](https://openjdk.java.net/install/)

Transparency Result Aggregator Service è parte della suite di servizi per la verifica delle informazioni sulla
Trasparenza dei siti web delle Pubbliche amministrazioni italiane.
 
## Transparency Result Aggregator Service

Transparency Result Aggregator Service è il componente che si occupa di gestire i risultati delle verifiche 
sulla corrispondenza dei siti degli enti pubblici italiani in relazione al decreto legge 33/2013 
sulla transparenza, aggregando i risultati di validazione con altre informazioni sugli enti pubblici prelevate
da altri servizi.

Transparency Result Aggregator Service fornisce alcuni servizi REST utilizzabili in produzione per:

 - inserire, aggiornare e cancellare all'interno del servizio le informazioni di una verifica 
   effettuata su un sito web di una PA ed dei dati geografici degli enti pubblici
 - esportare in geoJson i risultati delle validazioni presenti arricchiti con la geolicazzazione degli enti

II servizi REST sono documentati tramite OpenAPI consultabile all'indirizzo /swagger-ui/index.html.
L'OpenAPI del servizio di devel è disponibile all'indirizzo https://dica33.ba.cnr.it/result-aggregator-service/swagger-ui/index.html.

Questo servizio ha due dipendenze per funzionare:
 - il [Result Service](https://github.com/cnr-anac/result-service) da cui leggere le info sulle verifiche
 - il [Public Site Service](https://github.com/cnr-anac/public-sites-service) da cui prelevare le info geografiche delle PA

L'indirizzo di entrambi questi servizi è da configura nel file [application.properties](https://github.com/cnr-anac/result-aggregator-service/blob/main/src/main/resources/application.properties) oppure tramite variabili d'ambiente
se avviato tramite Docker.

### Sicurezza

Gli endpoint REST di questo servizio sono protetti tramite autenticazione OAuth con Bearer Token.
E' necessario configurare l'idp da utilizzare per validare i token OAuth tramite le due proprietà
mostrare nell'esempio seguente:

```
    - spring.security.oauth2.resourceserver.jwt.issuer-uri=https://dica33.ba.cnr.it/keycloak/realms/trasparenzai
    - spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://dica33.ba.cnr.it/keycloak/realms/trasparenzai/protocol/openid-connect/certs
```

Per l'accesso in HTTP GET all'API è sufficiente essere autenticati, per gli endpoint accessibili
con PUT/POST/DELETE è necessario oltre che essere autenticati che il token OAuth contenga un 
role ADMIN o SUPERUSER.

Inoltre questo servizio interagisce con il _result_service_ e il _public_site_service_ per prelevare
i risultati da aggregare.

Per configurare il client REST che accede a questi due servizi è necessario configurare questi 
parametri nel docker-compose.yml, in particolare verificare client-id, client-secret e issuer-uri.

```
    # Generare un Service Account Oidc con questo client-id, oppure cambiare questo valore
    - spring.security.oauth2.client.registration.oidc.client-id=result-aggregator
    # Client Secret da generare nel Identity Provider e impostare qui
    - spring.security.oauth2.client.registration.oidc.client-secret=client_secret_da_generare
    # URL dell'issuer OIDC da impostare
    - spring.security.oauth2.client.provider.oidc.issuer-uri=https://dica33.ba.cnr.it/keycloak/realms/trasparenzai
    # - spring.security.oauth2.client.registration.oidc.authorization-grant-type=client_credentials #DEFAULT
    # - spring.security.oauth2.client.registration.oidc.scope=openid #DEFAULT
    # - spring.security.oauth2.client.registration.oidc.provider=oidc #DEFAULT
```

# <img src="https://www.docker.com/wp-content/uploads/2021/10/Moby-logo-sm.png" width=80> Startup

#### _Per avviare una istanza del result-service con postgres locale_

Il result-aggregator-service può essere facilmente installato via docker compose su server Linux utilizzando il file 
docker-compose.yml presente in questo repository.

Accertati di aver installato docker e il plugin di docker `compose` dove vuoi installare il result-aggregator-service e 
in seguito esegui il comando successivo per un setup di esempio.

```
curl -fsSL https://raw.githubusercontent.com/cnr-anac/result-aggregator-service/main/first-setup.sh -o first-setup.sh && sh first-setup.sh
```

Collegarsi a http://localhost:8081/swagger-ui/index.html per visualizzare la documentazione degli endpoint REST presenti nel servizio.

**Attenzione**: se il public-site-service o il result-service non sono avviati sullo stesso server tramite docker è necessario
configurare l'url a cui rispondono, modificando le variabili d'ambiente *TRANSPARENCY_PUBLIC_SITE_URL* e *TRANSPARENCY_RESULT_SERVICE_URL* nel file *.env* e riavviare i container.

## Backups

Il servizio mantiene le informazioni relative alla configurazione nel db postgres, quindi è opportuno fare il backup
del database a scadenza regolare. Nel repository è presente un file di esempio [backups.sh](https://github.com/cnr-anac/result-aggregator-service/blob/main/backups.sh) per effettuare i backup.

All'interno dello script backups.sh è necessario impostare il corretto path dove si trova il docker-compose.yml del progetto, tramite la variabile `SERVICE_DIR`.

## 👏 Come Contribuire 

E' possibile contribuire a questo progetto utilizzando le modalità standard della comunità opensource 
(issue + pull request) e siamo grati alla comunità per ogni contribuito a correggere bug e miglioramenti.

## 📄 Licenza

Transparency Result Aggregator Service è concesso in licenza GNU AFFERO GENERAL PUBLIC LICENSE, come si trova 
nel file [LICENSE][l].

[l]: https://github.com/cnr-anac/result-aggregator-service/blob/master/LICENSE
