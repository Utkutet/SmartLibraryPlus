#  SmartLibraryPlus - ORM Tabanlı Akıllı Kütüphane Sistemi

Bu proje, **Nesneye Yönelik Programlama (OOP)** prensipleri ve **Hibernate ORM** teknolojisi kullanılarak geliştirilmiş, konsol tabanlı bir kütüphane yönetim sistemidir.

Projenin temel amacı; JDBC ile manuel SQL yazmadan, **Hibernate** üzerinden veritabanı nesne ilişkilerini (Entity-Relationship) yönetmek ve **CRUD** işlemlerini gerçekleştirmektir.

##  Kullanılan Teknolojiler

* **Dil:** Java (JDK 17+)
* **ORM:** Hibernate 6.x
* **Veritabanı:** SQLite (Gömülü veritabanı, kurulum gerektirmez)
* **Build Tool:** Maven
  
##  Proje Özellikleri

Bu sistem aşağıdaki temel işlevleri yerine getirir:

### 1. Kitap Yönetimi 
* Yeni kitap ekleme (Başlık, Yazar, Basım Yılı).
* Kitapları listeleme (Mevcut durumu ile: `MEVCUT` veya `ÖDÜNÇ VERİLDİ`).

### 2. Öğrenci Yönetimi 
* Öğrenci kaydı oluşturma (Ad, Bölüm).
* Kayıtlı öğrencileri listeleme.

### 3. Ödünç (Loan) Sistemi 
* **Ödünç Verme:** Bir kitabı bir öğrenciye ödünç verme.
    * *Kural:* Eğer kitap zaten başkasındaysa sistem izin vermez.
    * *Otomasyon:* İşlem başarılıysa kitabın durumu otomatik olarak `BORROWED` olur.
* **Listeleme:** Kimin hangi kitabı ne zaman aldığı listelenir.
* **İade Alma:** Kitap geri alındığında iade tarihi güncellenir ve kitap durumu tekrar `AVAILABLE` (Mevcut) olur.

##  Teknik Yapı ve Mimari

Proje, **DAO (Data Access Object)** tasarım deseni kullanılarak katmanlı bir yapıda geliştirilmiştir.

### Klasör Yapısı
```text
src/main/java/
  ├── app/         # Main sınıfı ve Konsol Menüsü
  ├── dao/         # Veritabanı CRUD işlemleri (Hibernate Session yönetimi)
  ├── entity/      # Veritabanı tablolarına karşılık gelen Java sınıfları
  └── util/        # Hibernate konfigürasyon ve SessionFactory yardımcısı
