package fr.supermax_8.endertranslate.core;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import fr.supermax_8.endertranslate.core.language.Language;
import fr.supermax_8.endertranslate.core.utils.Base64Utils;
import fr.supermax_8.endertranslate.core.utils.ResourceUtils;
import lombok.Data;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;

@Data
public class EnderTranslateConfig {

    @Getter
    private static EnderTranslateConfig instance;

    private static String data = "%%__USER__%% %%__RESOURCE__%% %%__NONCE__%%";

    private File dataFolder;

    private boolean mainServer;
    private String secret;
    private int wsPort;
    private String wsUrl;

    private String startTag;
    private String endTag;

    private String defaultLanguage;
    private LinkedHashMap<String, Language> languages = new LinkedHashMap<>();

    public EnderTranslateConfig(File dataFolder) {
        this.dataFolder = dataFolder;
        dataFolder.mkdirs();
        try {
            load(new File(dataFolder, "config.yml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        EnderTranslate.log("Config loaded");
        instance = this;
    }

    private void load(File configFile) throws IOException {
        boolean modified = false;

        YamlDocument config = YamlDocument.create(
                configFile,
                ResourceUtils.getResourceAsStream("config.yml"),
                GeneralSettings.builder().build(),
                LoaderSettings.builder().setAutoUpdate(true).build(),
                DumperSettings.builder().build(),
                UpdaterSettings.builder()
                        .setVersioning(new BasicVersioning("config-version"))
                        .addIgnoredRoute("0", Route.fromString("languages")).build()
        );


        mainServer = config.getBoolean("mainServer");
        secret = config.getString("secret");
        wsPort = config.getInt("wsPort");
        wsUrl = config.getString("wsUrl");

        startTag = config.getString("startTag");
        endTag = config.getString("endTag");

        defaultLanguage = config.getString("defaultLanguage");

        config.getSection("languages").getRouteMappedBlocks(false).forEach((route, block) -> {
            Section section = (Section) block;
            String title = section.getString("title");
            languages.put(section.getNameAsString(), new Language(section.getNameAsString(), title));
        });

        languages.keySet().forEach(s -> {
            EnderTranslate.log("Language loaded " + s);
        });

        if (secret == null || secret.isEmpty()) {
            secret = Base64Utils.generateSecuredToken(16);
            config.set("secret", secret);
            modified = true;
        }

        if (modified) config.save();
    }


    public void showConfiguration() {
        Class<?> classs = this.getClass();
        Field[] fields = classs.getDeclaredFields();

        System.out.println("Instance of class " + classs.getName());
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(this);
                System.out.println(field.getName() + ": " + value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

}