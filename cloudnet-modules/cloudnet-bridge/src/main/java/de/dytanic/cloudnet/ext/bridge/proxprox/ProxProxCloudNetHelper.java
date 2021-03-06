package de.dytanic.cloudnet.ext.bridge.proxprox;

import de.dytanic.cloudnet.common.Validate;
import de.dytanic.cloudnet.common.collection.Iterables;
import de.dytanic.cloudnet.common.collection.Maps;
import de.dytanic.cloudnet.driver.network.HostAndPort;
import de.dytanic.cloudnet.driver.service.ServiceEnvironmentType;
import de.dytanic.cloudnet.driver.service.ServiceInfoSnapshot;
import de.dytanic.cloudnet.ext.bridge.*;
import de.dytanic.cloudnet.ext.bridge.player.NetworkConnectionInfo;
import de.dytanic.cloudnet.ext.bridge.player.NetworkServiceInfo;
import de.dytanic.cloudnet.wrapper.Wrapper;
import io.gomint.proxprox.ProxProx;
import io.gomint.proxprox.api.entity.Player;
import io.gomint.proxprox.api.plugin.Plugin;
import io.gomint.proxprox.network.Protocol;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;

public final class ProxProxCloudNetHelper {

    public static final Map<String, ServiceInfoSnapshot> SERVER_TO_SERVICE_INFO_SNAPSHOT_ASSOCIATION = Maps.newConcurrentHashMap();

    public static ProxProx getProxyServer()
    {
        return ProxProx.instance;
    }

    private ProxProxCloudNetHelper()
    {
        throw new UnsupportedOperationException();
    }

    public static void initProperties(ServiceInfoSnapshot serviceInfoSnapshot)
    {
        Validate.checkNotNull(serviceInfoSnapshot);

        serviceInfoSnapshot.getProperties()
            .append("Online", true)
            .append("Online-Count", getProxyServer().getPlayers().size())
            .append("Default-Server", new HostAndPort(
                getProxyServer().getConfig().getDefaultServer().getIp(),
                getProxyServer().getConfig().getDefaultServer().getPort())
            )
            .append("Players", Iterables.map(getProxyServer().getPlayers(), new Function<Player, ProxProxCloudNetPlayerInfo>() {
                @Override
                public ProxProxCloudNetPlayerInfo apply(Player player)
                {
                    return new ProxProxCloudNetPlayerInfo(
                        player.getUUID(),
                        player.getLocale(),
                        player.getName(),
                        player.getXboxId(),
                        new HostAndPort(player.getAddress()),
                        player.getServer() != null ? new HostAndPort(player.getServer().getIP(), player.getServer().getPort()) : null,
                        player.getPing()
                    );
                }
            }))
            .append("Plugins", Iterables.map(getProxyServer().getPluginManager().getPlugins(), new Function<Plugin, PluginInfo>() {
                @Override
                public PluginInfo apply(Plugin plugin)
                {
                    PluginInfo pluginInfo = new PluginInfo(
                        plugin.getMeta().getName(),
                        plugin.getMeta().getVersion().getMajor() + "." + plugin.getMeta().getVersion().getMinor()
                    );

                    pluginInfo.getProperties()
                        .append("description", plugin.getMeta().getDescription())
                        .append("main-class", plugin.getClass().getName())
                        .append("depends", plugin.getMeta().getDepends())
                    ;

                    return pluginInfo;
                }
            }))
        ;
    }

    public static boolean isServiceEnvironmentTypeProvidedForProxProx(ServiceInfoSnapshot serviceInfoSnapshot)
    {
        Validate.checkNotNull(serviceInfoSnapshot);
        return serviceInfoSnapshot.getServiceId().getEnvironment().isMinecraftBedrockServer();
    }

    public static ServiceInfoSnapshot getServiceInfoSnapshotByHostAndPort(String host, int port)
    {
        Validate.checkNotNull(host);

        return Iterables.first(SERVER_TO_SERVICE_INFO_SNAPSHOT_ASSOCIATION.values(), new Predicate<ServiceInfoSnapshot>() {
            @Override
            public boolean test(ServiceInfoSnapshot serviceInfoSnapshot)
            {
                return serviceInfoSnapshot.getAddress().getHost().equalsIgnoreCase(host) && serviceInfoSnapshot.getAddress().getPort() == port;
            }
        });
    }

    public static NetworkConnectionInfo createNetworkConnectionInfo(Player player)
    {
        return BridgeHelper.createNetworkConnectionInfo(
            player.getUUID(),
            player.getName(),
            Protocol.MINECRAFT_PE_PROTOCOL_VERSION,
            new HostAndPort(player.getAddress()),
            new HostAndPort(getProxyServer().getConfig().getIp(), getProxyServer().getConfig().getPort()),
            true,
            false,
            new NetworkServiceInfo(
                ServiceEnvironmentType.PROX_PROX,
                Wrapper.getInstance().getServiceId().getUniqueId(),
                Wrapper.getInstance().getServiceId().getName()
            )
        );
    }

    public static String filterServiceForPlayer(Player player, String currentServer)
    {
        for (ProxyFallbackConfiguration proxyFallbackConfiguration : BridgeConfigurationProvider.load().getBungeeFallbackConfigurations())
            if (proxyFallbackConfiguration.getTargetGroup() != null && Iterables.contains(
                proxyFallbackConfiguration.getTargetGroup(),
                Wrapper.getInstance().getCurrentServiceInfoSnapshot().getConfiguration().getGroups()
            ))
            {
                List<ProxyFallback> proxyFallbacks = Iterables.newArrayList(proxyFallbackConfiguration.getFallbacks());
                Collections.sort(proxyFallbacks);

                String server = null;

                for (ProxyFallback proxyFallback : proxyFallbacks)
                {
                    if (proxyFallback.getTask() != null) continue;
                    if (proxyFallback.getPermission() != null && !player.hasPermission(proxyFallback.getPermission()))
                        continue;

                    List<Map.Entry<String, ServiceInfoSnapshot>> entries = getFilteredEntries(proxyFallback.getTask(), currentServer);

                    if (entries.size() == 0) continue;

                    server = entries.get(new Random().nextInt(entries.size())).getKey();
                }

                if (server == null)
                {
                    List<Map.Entry<String, ServiceInfoSnapshot>> entries = getFilteredEntries(proxyFallbackConfiguration.getDefaultFallbackTask(), currentServer);

                    if (entries.size() > 0)
                        server = entries.get(new Random().nextInt(entries.size())).getKey();
                }

                return server;
            }

        return null;
    }

    public static List<Map.Entry<String, ServiceInfoSnapshot>> getFilteredEntries(String task, String currentServer)
    {
        return Iterables.filter(
            SERVER_TO_SERVICE_INFO_SNAPSHOT_ASSOCIATION.entrySet(), new Predicate<Map.Entry<String, ServiceInfoSnapshot>>() {

                @Override
                public boolean test(Map.Entry<String, ServiceInfoSnapshot> stringServiceInfoSnapshotEntry)
                {
                    if (currentServer != null && currentServer.equalsIgnoreCase(stringServiceInfoSnapshotEntry.getKey()))
                        return false;

                    return task.equals(stringServiceInfoSnapshotEntry.getValue().getServiceId().getTaskName());
                }
            });
    }
}