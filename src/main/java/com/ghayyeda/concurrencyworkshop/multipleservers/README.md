# Wykorzystanie CompletableFuture

Rozwiąż to zadanie przy pomocy CompletableFuture.

Klasa *DataFetcher* odpowiada za pobranie danych z dwóch serwerów.

- chcemy uzyc odpowiedzi z tego serwera, który odpowie szybciej
- jeśli wszystkie serwery odpowiadają błędem (metoda *Server::fetchData* rzuca wyjątek), chcemy zwrócic dane z cache'a (*cachedData*)
- jeśli wszystkie serwery odpowiadają dłużej niż 1 sekundę, chcemy zwrócic dane z cache'a

Wszystkie wywołania serwerów chcemy wykonywa asynchronicznie, w puli wątków, nad którą mamy kontrolę.

Dane zwracane przez serwery są surowe (tablice byte'ów), więc przed zwróceniem odpowiedzi musimy je przetransformowa wykorzystując metodę
*DataTransformer::transform*. Operację transformacji chcemy wykonac asynchronicznie. 