## HwNodeAppTemplate Library

Bu kütüphane customer app'in, wifi ile iotignite'a bağlanması için yardımcı apiler içermektedir.
Kütüphanede temel olarak network service discovery, tcp server, iotignite bağlantı yardımcısı bulunmaktadır.
Kütüphaneyi **.aar** olarak Android Studio'da oluşturduğunuz projeye ekleyin.

![Image of HwNodeAppTemplate](HwNodeAppTemplate Library Sequence.png)

WifiNodeDevice sınıfı wifi ile iotignite'a bağlanacak cihazlar için temel fonksiyonları içermektedir. Bu kütüphaneyi kullanarak aşağıdaki olayları kolay bir şekilde ele alabilirsiniz :

 - DNS'e belirli mDNS servisi ekleyerek nodların gateway'in ip/portunu dinamik çözmesini sağlama (Network Service Discovery Server)
 - Ağa bağlı nodların oluşturacağı soket bağlantılarını dinleyen server(Tcp Server)
 - Nod verilerini okuyup yazmak için kullanılan istemci soketi(Tcp Client)
 - Uygulama servis olarak yazılmışsa yeniden başlatma sonrasında otomatik ayaklanmasını sağlama (Boot Completed Receiver)
 - Wifi bağlantısıdna oluşan kopma bağlanamama yeniden bağlanma durumlarını algılama ve ona göre etkilenen servisleri yeniden başlatma (Connection State Changed Receiver)
 - Ignite Agent ile Customer App arasında IgniteSDK versiyonundan kaynaklanan bir uyumsuzluk varsa bu uyumsuzluğu ele alan yardımcı sınıf (Ignite SDK Compatibility Listener)
 - Soket & Ignite bağlantısının kopması durumlarını ele alma (Socket & Ignite Connection Listener)
 - Verilerin Ignite'a gönderilmesi, action, konfigürasyon alınması



![Image of HwNodeAppTemplate](hwNodeAppTemplateComponents.png)


Kütüphanenin nasıl kullanıldığına dair örnek olarak _**IotIgnite Dynamic Node Example**_ örnek uygulamasına bakabilirsiniz.
