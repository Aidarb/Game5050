package aidarb;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Client implements Callable<String> {

    private int successRequest = 0; //количество успешных запросов
    private int wrongRequest = 0; //количество неуспешных запросов
    private long averageRequestTime = 0; //среднее время запроса
    private int bet; //ставка клиента
    private boolean coinSide; //решка = true, орел = false
    private int coins; //количество жетонов
    private Socket clientSocket; //сокет клиента
    private DataInputStream in; // поток чтения из сокета
    private DataOutputStream out; // поток записи в сокет

    private int secOfDelay; // интервал обращений к серверной стороне клиента
    private int id; // идентификатор клиента
    private int count; // количество обращений клиента
    private int num;

    public Client(int secOfDelay, int count) {
        try {
            clientSocket = new Socket("localhost", 8080); // доступ к серверу
        } catch (IOException e) {
            System.out.println("Нет подключения к серверу");
        }
        this.secOfDelay = secOfDelay;
        this.count = count;
        this.num = count;
    }

    @Override
    public String call() throws Exception {
        try {
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());

            String serverWord;
            long startTime;
            ThreadLocalRandom random = ThreadLocalRandom.current();

            try {
                id = in.readInt(); // чтение идентификатора клиента
            } catch (IOException e){
                //не включено в список обращений, исключение не будет увеличивать количество неуспешных запросов.
                e.printStackTrace();
            }

            //цикл обращений к серверу
            while (!clientSocket.isClosed() && count > 0) {

                startTime = System.nanoTime(); // запишем начало цикла обращения

                try {
                    coins = in.readInt(); // чтение с сервера количество жетонов
                    coinSide = random.nextBoolean(); //случайная сторона true = решка, false = орел
                    bet = random.nextInt(coins + 1); // случайная ставка в пределах количества жетонов
                    out.writeBoolean(coinSide); //отправляем сторону жетона на сервер
                    out.writeInt(bet);// отправляем ставку на сервер
                    out.flush();
                    serverWord = in.readUTF(); // ответ сервера о результате игры
                } catch (IOException e){
                    wrongRequest++; //неуспешный запрос
                }

                averageRequestTime += System.nanoTime() - startTime; //запись длительности каждого запроса в переменную
                successRequest++; // количество успешных запросов
                count--;

                // интервал между обращениями к серверной стороне одного пользователя
                TimeUnit.SECONDS.sleep(secOfDelay);
            }

            averageRequestTime /= num; // средняя длительность запроса

        } catch (InterruptedException e) {
            wrongRequest++; //неуспешный запрос
        } finally {
            successRequest -= wrongRequest; // вычтем из всех запросов прошедших цикл неуспешные.
            // в любом случае необходимо закрыть сокет и потоки
            in.close();
            out.close();
            clientSocket.close();
        }

        return "Клиент " + id + " | " +  successRequest + " | " + wrongRequest + " | " + averageRequestTime;
    }
}
