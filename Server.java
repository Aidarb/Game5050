package aidarb;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Server implements Runnable {

    private int id;
    private int bet;
    private int percent = 50; //шанс выпадения решки
    private int coins = 100; //количество жетонов
    private boolean coinSide; // решка = true, орел = false
    private Socket socket; // сокет, через который сервер общается с клиентом
    private DataInputStream in;
    private DataOutputStream out;

    private ArrayList<String> gameStory = new ArrayList<>(); //связанный список для хранения истории игр игрока

    private boolean coinGame() {

        ThreadLocalRandom random = ThreadLocalRandom.current();

        if (random.nextInt(100) < percent)
            return true;

        return false;
    }

    public Server(int id, Socket socket) {
        this.id = id;
        this.socket = socket;
    }

    @Override
    public void run() {

        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            String serverWord;
            String winOrLose;
            String orelOrReshka;

            try {
                out.writeInt(id);
                out.flush();
            } catch (IOException e){
                System.out.println("Ошибка присвоения клиенту " + id + " его идентификатор");
            }

            while (!socket.isClosed()) {

                try {
                    out.writeInt(coins); //передача клиенту количества его жетонов
                    out.flush();
                    coinSide = in.readBoolean(); //сторона выбранная игроком
                    bet = in.readInt(); // ставка сделанная игроком
                } catch (IOException e){
                    System.out.println("Ошибка передачи клиенту " + id + " количества его жетонов и данных о его ставке");
                    break;
                }

                if (coins <= 0) {
                    try {
                        out.writeUTF("Клиент " + id + ". Жетоны закончились");
                        System.out.println("Клиент " + id + ". Жетоны закончились");
                        out.flush();
                    } catch (IOException e){
                        System.out.println("Ошибка передачи клиенту " + id + " сообщения");
                    }
                    break;
                }

                if (bet == 0) {
                    try {
                        out.writeUTF("Клиент " + id + ". Нулевая ставка");
                        System.out.println("Клиент " + id + ". Нулевая ставка");
                        out.flush();
                    } catch (IOException e){
                        System.out.println("Ошибка передачи клиенту " + id + " сообщения");
                    }
                    continue;
                }

                //если ставка больше количества
                if ((coins - bet) < 0) {
                    try {
                        out.writeUTF("Клиент " + id + ". Ставка больше жетонов");
                        System.out.println("Клиент " + id + ". Ставка больше жетонов");
                        out.flush();
                    } catch (IOException e){
                        System.out.println("Ошибка передачи клиенту " + id + " сообщения");
                    }
                    continue;
                }

                if (coinGame() == coinSide) {
                    coins += bet * 1.9;
                    winOrLose = "победил";
                } else {
                    coins -= bet;
                    winOrLose = "проиграл";
                }

                orelOrReshka = (coinSide == true) ? "решка" : "орел";

                serverWord = String.format("Клиент %s. Ставка: %s. " +
                        "Сторона: %s. Ты %s! " +
                        "Жетонов: %s", id, bet, orelOrReshka, winOrLose, coins);
                try {
                    out.writeUTF(serverWord);
                    out.flush();
                    gameStory.add(serverWord);
                    System.out.println(serverWord);
                } catch (IOException e){
                    System.out.println("Ошибка передачи клиенту " + id + " сообщения");
                }
            }
            System.out.println("Сервер клиента " + id + " закрыт");
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("Ошибка потока ввода и вывода");
        }
    }
}