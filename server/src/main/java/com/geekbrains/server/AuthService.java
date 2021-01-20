package com.geekbrains.server;

public interface AuthService {

    String getNicknameByLoginAndPassword(String login, String password);
    boolean updateNickname(String oldNickname, String newNickname);
}
