package net.azisaba.healthchecker.yaml;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class YamlConfiguration {
    public static final Yaml DEFAULT = new Yaml();

    private final Yaml yaml;
    private final Object data;

    public YamlConfiguration(@NotNull String path) throws FileNotFoundException { this(new File(path)); }

    public YamlConfiguration(@NotNull File file) throws FileNotFoundException {
        if (!file.exists() || !file.isFile()) throw new FileNotFoundException(file.getName() + " does not exist or is not a file");
        this.yaml = DEFAULT;
        this.data = this.yaml.load(new FileInputStream(file));
    }

    public YamlConfiguration(@NotNull Yaml yaml, @NotNull InputStream inputStream) {
        this.yaml = yaml;
        this.data = this.yaml.load(inputStream);
    }

    public YamlConfiguration(@NotNull Yaml yaml, @NotNull String yamlInString) {
        this.yaml = yaml;
        this.data = this.yaml.load(yamlInString);
    }

    public YamlConfiguration(@NotNull InputStream inputStream) {
        this(DEFAULT, inputStream);
    }

    /**
     * Parses object as Yaml object.
     * @return the parsed yaml object
     * @throws ClassCastException when the object is not valid yaml object
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public YamlObject asObject() throws ClassCastException { return new YamlObject(yaml, (Map<String, Object>) data); }

    /**
     * Parses object as Yaml array.
     * @return the parsed yaml array
     * @throws ClassCastException when the object is not valid yaml array
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public YamlArray asArray() throws ClassCastException { return new YamlArray((List<Object>) data); }

    /**
     * Returns raw data returned from the yaml parser.
     * @return raw data
     */
    @NotNull
    public Object getData() { return Objects.requireNonNull(data); }

    public void save(@NotNull File file, @NotNull YamlMember member) throws IOException {
        saveTo(file, yaml, member);
    }

    public void save(@NotNull String path, @NotNull YamlMember member) throws IOException {
        saveTo(new File(path), yaml, member);
    }

    @Contract(pure = true)
    @NotNull
    public String save(@NotNull YamlMember member) {
        return dump(member);
    }

    @NotNull
    public Yaml getYaml() { return yaml; }

    public static void saveTo(@NotNull File file, @NotNull Yaml yaml, @NotNull YamlMember member) throws IOException {
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        out.write(yaml.dumpAsMap(member.getRawData()).getBytes(StandardCharsets.UTF_8));
        out.close();
    }

    public static void saveTo(@NotNull File file, @NotNull YamlMember member) throws IOException {
        saveTo(file, DEFAULT, member);
    }

    public static void saveTo(@NotNull String path, @NotNull Yaml yaml, @NotNull YamlMember member) throws IOException {
        saveTo(new File(path), yaml, member);
    }

    @Contract(pure = true)
    @NotNull
    public static String dump(@NotNull YamlMember member) {
        return DEFAULT.dumpAsMap(member.getRawData());
    }
}
