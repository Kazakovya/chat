package com.geekbrains.server;

public class SimpleAuthService implements AuthService {

    private final DBHelper dbHelper = DBHelper.getInstance();

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        return dbHelper.findByLoginAndPassword(login, password);
    }

    @Override
    public boolean updateNickname(String oldNickname, String newNickname) {
        int result = dbHelper.updateNickname(oldNickname, newNickname);

        return result == 1;
    }
}

