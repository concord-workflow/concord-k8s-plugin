package ca.vanzyl.concord.plugins.k8s.secrets;

import ca.vanzyl.concord.plugins.ImmutablesYamlMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class SecretsManager {

    private final ImmutablesYamlMapper mapper;

    public SecretsManager() {
        this.mapper = new ImmutablesYamlMapper();
    }

    public List<Secret> load(String input) throws IOException {
        return mapper.read(input, Secrets.class).list();
    }

    public List<Secret> load(File input) throws IOException {
        return mapper.read(input, Secrets.class).list();
    }

    public List<Secret> load(InputStream input) throws IOException {
        return mapper.read(input, Secrets.class).list();
    }
}
