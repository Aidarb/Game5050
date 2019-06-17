package aidarb;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class TestClient {

    public static int numOfPull = 30; // количество одновременно играющих пользователей
    public static int secOfDelay = 0; // интервал между обращениями к серверной стороне одного пользователя в секундах
    public static int numOfRequests = 10; // количество обращений к игровому серверу одного пользователя

    public static void main(String[] args){

        //создание сервиса для пула потоков
        ExecutorService executorService = Executors.newFixedThreadPool(numOfPull);

        //Обьявление объекта и списка обьектов Callable
        Callable<String> callable;
        ArrayList<Callable<String>> callables = new ArrayList<>();

        //создание списка обьектов Future
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < numOfPull; i++){
            callable = new Client(secOfDelay, numOfRequests);
            callables.add(callable); //добавление таска в список
        }

        try {
            //запуск объектов Callable, возаращение объетков Future
            futures = executorService.invokeAll(callables);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // вывод таблицы данных
        for (Future<String> futureResult : futures){
            try {
                System.out.println(futureResult.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        // закрываем фабрику
        executorService.shutdown();
    }
}