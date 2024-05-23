# Transparency Result Aggregator Service
## Transparency Result Aggregator - REST Services

[![Supported JVM Versions](https://img.shields.io/badge/JVM-11-brightgreen.svg?style=for-the-badge&logo=Java)](https://openjdk.java.net/install/)

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

## 👏 Come Contribuire 

E' possibile contribuire a questo progetto utilizzando le modalità standard della comunità opensource 
(issue + pull request) e siamo grati alla comunità per ogni contribuito a correggere bug e miglioramenti.

## 📄 Licenza

Transparency Result Aggregator Service è concesso in licenza GNU AFFERO GENERAL PUBLIC LICENSE, come si trova 
nel file [LICENSE][l].

[l]: https://github.com/cnr-anac/result-aggregator-service/blob/master/LICENSE
