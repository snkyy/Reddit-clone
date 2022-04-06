package forum.web;

// interfejs do procesora szablonów;
// jest on odpowiedzialny za generowanie zawartości strony na podstawie szablonu i konkretnych danych;
// konkretne dane są obiektem z pakietu forum.views;
// nazwy szablonów, które muszą być zrealizowane to nazwy klas z pakietu forum.views zamienione z CamelCase na hyphen-case;
// wygląd strony i język szablonów jest częścią implementacji
public interface TemplateProcessor {
    String process(String template, Object data); // nazwa szablonu i konkretne dane
}
