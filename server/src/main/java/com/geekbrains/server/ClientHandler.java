package com.geekbrains.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private String nickname;
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public String getNickname() {
        return nickname;
    }

    public ClientHandler(Server server, Socket socket, AuthService authService) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    waitAutorization(server);
                    waitMessageOrCommand(server, authService);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    ClientHandler.this.disconnect();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void waitAutorization(Server server) throws IOException {
        while (true) {
            String msg = in.readUTF();
            // /auth login1 pass1
            if (msg.startsWith("/auth ")) {
                String[] tokens = msg.split("\\s");
                String nick = server.getAuthService().getNicknameByLoginAndPassword(tokens[1], tokens[2]);

                if (nick != null && !server.isNickBusy(nick)) {
                    sendMsg("/authok " + nick);
                    nickname = nick;
                    server.subscribe(this);
                    break;
                }
            }
        }
    }

    private void waitMessageOrCommand(Server server, AuthService authService) throws IOException {
        while (true) {
            String msg = in.readUTF();

            if (msg.startsWith("/")) {
                if (msg.equals("/end")) {
                    sendMsg("/end");
                    break;
                }

                checkPrivateMessageCommand(server, msg);
                checkUpdateNicknameCommand(server, authService, msg);
            } else {
                server.broadcastMsg(nickname + ": " + msg);
            }
        }
    }

    private void checkPrivateMessageCommand(Server server, String msg) {
        if (msg.startsWith("/w ")) {
            String[] tokens = msg.split("\\s", 3);
            server.privateMsg(this, tokens[1], tokens[2]);
        }
    }

    private void checkUpdateNicknameCommand(Server server, AuthService autService, String msg) {
        if (msg.startsWith("/upNick ")) {
            String[] tokens = msg.split("\\s", 2);

            if (tokens.length == 2 && !tokens[1].trim().equals("")) {
                String newNickname = tokens[1].trim();

                if (!server.isNickBusy(newNickname) && autService.updateNickname(getNickname(), newNickname)) {
                    String message = String.format("Имя пользователя \"%s\" было заменено на \"%s\"", getNickname(), newNickname);
                    server.broadcastMsg(message);

                    nickname = newNickname;
                    server.broadcastClientsList();
                }
            }
        }
    }


    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        server.unsubscribe(this);
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


