# Współbieżne pobieranie danych z wielu źródeł

Twoim zadaniem jest zaimplementowanie metody *getExchangeRate()* w klasie *ConcurrentExchangeRateProvider*.

1. Kurs wymiany waluty zwracany przez *ConcurrentExchangeRateProvider* ma być wyliczony jako średni kurs wymiany z czterech różnych banków. Repozytoria 1..4 symbolizują wywołania API poszczególnych banków.
2. Klasa *ConcurrentExchangeRateProvider* w celu zwrócenia kursu waluty musi wywołać metody *getExchangeRate()* na repozytoriach nr 1, 2, 3 i 4. Repozytoria są przekazywane w konstruktorze. 
2. Repozytoria mają zostać wywołane współbieżnie, w celu zmniejszenia całościowego czasu odpowiedzi. Niestety, ze względu na kwestię licencyjne API, ogranicza nas limit równoległych żądań  do każdego z banku (repozytorium). Każdy z banków ma inny limit - limity te są przekazane w konstruktorze. W przypadku przekroczenia tego limitu otrzymamy słony rachunek, więc za wszelką cenę chcemy tego uniknać.

   - W momencie gdy mielibyśmy przekroczyc limit, chcemy natychmiast zwolnić klientów klasy
   *ConcurrentExchangeRateProvider* z dalszego oczekiwania poprzez rzucenie *TooManyRequestsException*
   - Metoda *getExchangeRate()* zwraca *Future<Double>*, więc samo wywołanie tej metody nie powinno blokowac wątku wywołującego
   - Czas pobrania średniego kursy wymiany nie powinien znacząco odbiegać od czasu odpowiedzi najwolniejszego banku (repozytorium).
   - Chcemy zapewnić maksymalną przepustowość, ale nie chcemy przekraczać limitu.
3. Nie chcemy cache'ować danych, bo zbyt często się zmieniają.
4. Komponent ConcurrentExchangeRateProvider będzie używany w środowisku wielowątkowym - wiele wątków jednocześnie będzie próbowa pobierac aktualny kurs wymiany
5. Nie skupiaj się na designie kodu, na typowych praktykach programistycznych - popracuj za to nad poprawnością rozwiązania pod kątem współbieżności

