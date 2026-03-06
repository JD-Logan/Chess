package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;

public class UserService {

    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData register(UserData user) throws DataAccessException {
        if (user.username() == null || user.password() == null || user.email() == null) {
            throw new IllegalArgumentException("missing fields");
        }
        if (dataAccess.getUser(user.username()) != null) {
            throw new IllegalStateException("username taken");
        }
        dataAccess.createUser(user);
        return dataAccess.createAuth(user.username());
    }

    public AuthData login(String username, String password) throws DataAccessException {
        if (username == null || password == null) {
            throw new IllegalArgumentException("missing fields");
        }
        UserData user = dataAccess.getUser(username);
        if (user == null || !user.password().equals(password)) {
            throw new SecurityException("unauthorized");
        }
        return dataAccess.createAuth(username);
    }

    public void logout(String authToken) throws DataAccessException {
        if (dataAccess.getAuth(authToken) == null) {
            throw new SecurityException("unauthorized");
        }
        dataAccess.deleteAuth(authToken);
    }
}


