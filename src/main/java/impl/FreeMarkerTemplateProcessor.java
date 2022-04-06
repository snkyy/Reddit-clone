package impl;

import forum.web.TemplateProcessor;
import freemarker.template.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Optional;
import java.util.OptionalLong;

// procesor szablonów FreeMarker
public class FreeMarkerTemplateProcessor implements TemplateProcessor {
    private final Configuration config;
    
    public FreeMarkerTemplateProcessor() {
        config = new Configuration(Configuration.VERSION_2_3_31);
        config.setClassForTemplateLoading(getClass(), "/templates"); // szablony znajdują się w zasobach projektu
        config.setObjectWrapper(new Wrapper()); // poprawka na specjalną obsługę obiektów typu Optional<> i OptionalLong
        config.setDateTimeFormat("yyyy-MM-dd HH:mm");
    }

    @Override
    public String process(String templateName, Object data) {
        try {
            var template = config.getTemplate(templateName + ".ftlh"); // FreeMarker cache'uje szablony
            var writer = new StringWriter();
            template.process(data, writer);
            return writer.toString();
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    private class Wrapper extends DefaultObjectWrapper {
        public Wrapper() {
            super(config.getIncompatibleImprovements());
        }

        @Override
        protected TemplateModel handleUnknownType(Object obj) throws TemplateModelException {
            if (obj instanceof Optional)
                obj = ((Optional<?>) obj).orElse(null);
            if (obj instanceof OptionalLong)
                obj = ((OptionalLong) obj).stream().boxed().findAny().orElse(null);
            return super.handleUnknownType(obj);
        }
    }
}
