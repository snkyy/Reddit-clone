package forum.databases;

// interfejs bazy danych (tematycznie podzielony na cztery interfejsy);
// implementacja bazy danych jest odpowiedzialna za konstruowanie rekordów z pakietu forum.entities
public interface Database extends
        UsersDatabase,
        CommunitiesDatabase,
        PostsDatabase,
        CommentsDatabase { }
