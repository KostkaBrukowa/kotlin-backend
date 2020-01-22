# kotlin-backend

Jeżeli chodzi o stack programistyczny cały backend byłby napisany w Kotlinie wraz z Spring i Spring Boot, 
a frontend w React i Typescript.

Cała komunikacja pomiędzy frontendem a backendem odbywała by się poprzez GraphQL.

Byłaby to biznesowa aplikacja pomagają ludziom rozliczać się po wydarzeniach. Przykładowa sytuacja to:

Pewna grupa znajomych spotyka się i każdy z nich robi pewne wydatki.
Jedna osoba zamówi pizze, druga kupi napoje itd. W naszych czasach dosyć rzadko
ludzie noszą prawdziwe pieniądze przy sobie, więc nasi znajomi nie zrzucają się na produkty,
tylko jedna osoba zazwyczaj płaci, a po skończonej imprezie wszyscy musza się rozliczać.

W tym miejscu będzie pomagała moja aplikacja. Czyli przed rozpoczęciem imprezy użytkownik
będzie mógł założyć pewne wydarzenie w aplikacji. Do tego wydarzenia będzie mógł 
zapraszać osoby (które to osoby będą mogły to zaproszenie przyjąć albo odrzucić).

Aplikacja będzie udostępniała czat pomiędzy wybranymi osobami, jak i w danym wydarzeniu.
Podczas imprezy użytkownik będzie mógł tworzyć Wydatki, czyli pewne obiekty które zawierają ceny, 
informacje i osoby składające się i płacące za dana rzecz. Po utworzeniu wydatku będzie można
także dodawać nowe składające się osoby. Każdy użytkownik będzie mógł zgodzić się na wydatek lub się
nie zgodzić. Dodatkowo przewiduję dodawanie zdjęć np. paragonów dla potwierdzenia wydatku.
Gdy impreza się skończy zakładający może zakończyć ją z w aplikacji, a następnie każdy uczestnik otrzyma dokładnie
obliczone pieniądze jakie jest winien każdej innej osobie (oczywiście pomniejszone o to co ta druga osoba jest jemu winna). 

Dodatkowo proponowałbym połączenie z Google Maps, aby można było podać miejsce
wydarzenia i być może połączenie z jakimś bankiem (np. aplikacja MBank), aby płatności
można było dokonać nie przypisując wszystkich potrzebnych danych. 

Będą dodatkowo udostępnione podstawowe opcje, czyli wyszukiwarka (wydarzeń albo użytkowników),
logowanie, zarządzanie hasłem, zarządzanie profilem itp.
