package de.dytanic.cloudnet.template;

import de.dytanic.cloudnet.CloudNet;
import de.dytanic.cloudnet.command.commands.CommandLocalTemplate;
import de.dytanic.cloudnet.common.Validate;
import de.dytanic.cloudnet.common.io.FileUtils;
import de.dytanic.cloudnet.driver.service.ServiceEnvironmentType;
import de.dytanic.cloudnet.driver.service.ServiceTemplate;
import de.dytanic.cloudnet.util.InstallableAppVersion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public final class LocalTemplateStorageUtil {

    private LocalTemplateStorageUtil()
    {
        throw new UnsupportedOperationException();
    }

    public static LocalTemplateStorage getLocalTemplateStorage()
    {
        return (LocalTemplateStorage) CloudNet.getInstance().getServicesRegistry()
            .getService(ITemplateStorage.class, LocalTemplateStorage.LOCAL_TEMPLATE_STORAGE);
    }

    public static File[] getFilesFromDirectory(ServiceTemplate serviceTemplate, String directoryPath)
    {
        Validate.checkNotNull(serviceTemplate);

        File target = new File(getLocalTemplateStorage().getStorageDirectory() + "/" + serviceTemplate.getTemplatePath(), directoryPath);

        if (target.exists())
        {
            File[] files = target.listFiles();

            if (files != null) return files;
        }

        return null;
    }

    public static File getFile(ServiceTemplate serviceTemplate, String path)
    {
        return new File(getLocalTemplateStorage().getStorageDirectory() + "/" + serviceTemplate.getTemplatePath(), path);
    }

    public static boolean installApplicationJar(ITemplateStorage storage, ServiceTemplate serviceTemplate, InstallableAppVersion installableAppVersion)
    {
        Validate.checkNotNull(storage);
        Validate.checkNotNull(serviceTemplate);
        Validate.checkNotNull(installableAppVersion);

        if (!storage.has(serviceTemplate))
            try
            {
                createAndPrepareTemplate(storage, serviceTemplate.getPrefix(), serviceTemplate.getName(), installableAppVersion.getServiceEnvironment());
            } catch (Exception e)
            {
                e.printStackTrace();
            }

        return installApplicationJar0(
            installableAppVersion.getUrl(),
            new File(System.getProperty("cloudnet.storage.local", "local/templates") + "/" +
                serviceTemplate.getTemplatePath() + "/" + installableAppVersion.getEnvironmentType().getName() + ".jar")
        );
    }

    private static boolean installApplicationJar0(String url, File destination)
    {
        try
        {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();

            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setDoOutput(false);
            httpURLConnection.connect();

            if (!destination.exists())
                destination.createNewFile();

            try (InputStream inputStream = httpURLConnection.getInputStream();
                 FileOutputStream fileOutputStream = new FileOutputStream(destination))
            {
                FileUtils.copy(inputStream, fileOutputStream);
            }

            httpURLConnection.disconnect();

            return true;
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return false;
    }

    public static boolean createAndPrepareTemplate(ITemplateStorage storage, String prefix, String name, ServiceEnvironmentType environment) throws Exception
    {
        Validate.checkNotNull(storage);
        Validate.checkNotNull(prefix);
        Validate.checkNotNull(name);
        Validate.checkNotNull(environment);

        ServiceTemplate serviceTemplate = new ServiceTemplate(prefix, name, LocalTemplateStorage.LOCAL_TEMPLATE_STORAGE);

        if (!storage.has(serviceTemplate))
        {
            File directory = new File(System.getProperty("cloudnet.storage.local", "local/templates") + "/" + serviceTemplate.getTemplatePath());
            directory.mkdirs();

            new File(directory, "plugins").mkdirs();
            byte[] buffer = new byte[3072];

            switch (environment)
            {
                case PROX_PROX:
                {
                    File proxyYml = new File(directory, "config.yml");
                    proxyYml.createNewFile();

                    try (FileOutputStream fileOutputStream = new FileOutputStream(proxyYml);
                         InputStream inputStream = CommandLocalTemplate.class.getClassLoader().getResourceAsStream("files/proxprox/config.yml"))
                    {
                        if (inputStream != null) FileUtils.copy(inputStream, fileOutputStream, buffer);
                    }
                }
                break;
                case BUNGEECORD:
                {
                    File proxyYml = new File(directory, "config.yml");
                    proxyYml.createNewFile();

                    try (FileOutputStream fileOutputStream = new FileOutputStream(proxyYml);
                         InputStream inputStream = CommandLocalTemplate.class.getClassLoader().getResourceAsStream("files/bungee/config.yml"))
                    {
                        if (inputStream != null) FileUtils.copy(inputStream, fileOutputStream, buffer);
                    }

                    File serverIcon = new File(directory, "server-icon.png");
                    serverIcon.createNewFile();

                    try (FileOutputStream fileOutputStream = new FileOutputStream(serverIcon);
                         InputStream inputStream = CommandLocalTemplate.class.getClassLoader().getResourceAsStream("files/server-icon.png"))
                    {
                        if (inputStream != null) FileUtils.copy(inputStream, fileOutputStream, buffer);
                    }
                }
                break;
                case VELOCITY:
                {
                    File proxyYml = new File(directory, "velocity.toml");
                    proxyYml.createNewFile();

                    try (FileOutputStream fileOutputStream = new FileOutputStream(proxyYml);
                         InputStream inputStream = CommandLocalTemplate.class.getClassLoader().getResourceAsStream("files/velocity/velocity.toml"))
                    {
                        if (inputStream != null) FileUtils.copy(inputStream, fileOutputStream, buffer);
                    }

                    File serverIcon = new File(directory, "server-icon.png");
                    serverIcon.createNewFile();

                    try (FileOutputStream fileOutputStream = new FileOutputStream(serverIcon);
                         InputStream inputStream = CommandLocalTemplate.class.getClassLoader().getResourceAsStream("files/server-icon.png"))
                    {
                        if (inputStream != null) FileUtils.copy(inputStream, fileOutputStream, buffer);
                    }
                }
                break;
                case NUKKIT:
                {
                    File serverProperties = new File(directory, "server.properties");
                    serverProperties.createNewFile();

                    try (FileOutputStream fileOutputStream = new FileOutputStream(serverProperties);
                         InputStream inputStream = CommandLocalTemplate.class.getClassLoader().getResourceAsStream("files/nukkit/server.properties"))
                    {
                        if (inputStream != null) FileUtils.copy(inputStream, fileOutputStream, buffer);
                    }

                    File nukkitYml = new File(directory, "nukkit.yml");
                    nukkitYml.createNewFile();

                    try (FileOutputStream fileOutputStream = new FileOutputStream(nukkitYml);
                         InputStream inputStream = CommandLocalTemplate.class.getClassLoader().getResourceAsStream("files/nukkit/nukkit.yml"))
                    {
                        if (inputStream != null) FileUtils.copy(inputStream, fileOutputStream, buffer);
                    }
                }
                break;
                case GO_MINT:
                {
                    File serverYml = new File(directory, "server.yml");
                    serverYml.createNewFile();

                    try (FileOutputStream fileOutputStream = new FileOutputStream(serverYml);
                         InputStream inputStream = CommandLocalTemplate.class.getClassLoader().getResourceAsStream("files/gomint/server.yml"))
                    {
                        if (inputStream != null) FileUtils.copy(inputStream, fileOutputStream, buffer);
                    }
                }
                break;
                case MINECRAFT_SERVER:
                {
                    File serverProperties = new File(directory, "server.properties");
                    serverProperties.createNewFile();

                    try (FileOutputStream fileOutputStream = new FileOutputStream(serverProperties);
                         InputStream inputStream = CommandLocalTemplate.class.getClassLoader().getResourceAsStream("files/nms/server.properties"))
                    {
                        if (inputStream != null) FileUtils.copy(inputStream, fileOutputStream, buffer);
                    }

                    File bukkitYml = new File(directory, "bukkit.yml");
                    bukkitYml.createNewFile();

                    try (FileOutputStream fileOutputStream = new FileOutputStream(bukkitYml);
                         InputStream inputStream = CommandLocalTemplate.class.getClassLoader().getResourceAsStream("files/nms/bukkit.yml"))
                    {
                        if (inputStream != null) FileUtils.copy(inputStream, fileOutputStream, buffer);
                    }

                    File spigotYml = new File(directory, "spigot.yml");
                    spigotYml.createNewFile();

                    try (FileOutputStream fileOutputStream = new FileOutputStream(spigotYml);
                         InputStream inputStream = CommandLocalTemplate.class.getClassLoader().getResourceAsStream("files/nms/spigot.yml"))
                    {
                        if (inputStream != null) FileUtils.copy(inputStream, fileOutputStream, buffer);
                    }

                    File spongeGlobalConf = new File(directory, "config/sponge/global.conf");
                    spongeGlobalConf.getParentFile().mkdirs();
                    spongeGlobalConf.createNewFile();

                    try (FileOutputStream fileOutputStream = new FileOutputStream(spongeGlobalConf);
                         InputStream inputStream = CommandLocalTemplate.class.getClassLoader().getResourceAsStream("files/nms/global.conf"))
                    {
                        if (inputStream != null) FileUtils.copy(inputStream, fileOutputStream, buffer);
                    }

                    File serverIcon = new File(directory, "server-icon.png");
                    serverIcon.createNewFile();

                    try (FileOutputStream fileOutputStream = new FileOutputStream(serverIcon);
                         InputStream inputStream = CommandLocalTemplate.class.getClassLoader().getResourceAsStream("files/server-icon.png"))
                    {
                        if (inputStream != null) FileUtils.copy(inputStream, fileOutputStream, buffer);
                    }
                }
                break;
                case GLOWSTONE:
                {
                    File glowstoneYml = new File(directory, "config/glowstone.yml");
                    glowstoneYml.getParentFile().mkdirs();
                    glowstoneYml.createNewFile();

                    try (FileOutputStream fileOutputStream = new FileOutputStream(glowstoneYml);
                         InputStream inputStream = CommandLocalTemplate.class.getClassLoader().getResourceAsStream("files/glowstone/glowstone.yml"))
                    {
                        if (inputStream != null) FileUtils.copy(inputStream, fileOutputStream, buffer);
                    }

                    File serverIcon = new File(directory, "server-icon.png");
                    serverIcon.createNewFile();

                    try (FileOutputStream fileOutputStream = new FileOutputStream(serverIcon);
                         InputStream inputStream = CommandLocalTemplate.class.getClassLoader().getResourceAsStream("files/server-icon.png"))
                    {
                        if (inputStream != null) FileUtils.copy(inputStream, fileOutputStream, buffer);
                    }
                }
                break;
            }

            CloudNet.getInstance().deployTemplateInCluster(serviceTemplate, storage.toZipByteArray(serviceTemplate));
            return true;
        } else
            return false;
    }
}