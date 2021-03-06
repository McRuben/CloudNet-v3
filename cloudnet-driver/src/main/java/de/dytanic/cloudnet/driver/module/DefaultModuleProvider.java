package de.dytanic.cloudnet.driver.module;

import de.dytanic.cloudnet.common.Validate;
import de.dytanic.cloudnet.common.collection.Iterables;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

public final class DefaultModuleProvider implements IModuleProvider {

    protected Collection<DefaultModuleWrapper> moduleWrappers = Iterables.newCopyOnWriteArrayList();

    @Getter
    @Setter
    protected IModuleProviderHandler moduleProviderHandler = new ModuleProviderHandlerAdapter();

    @Getter
    @Setter
    protected IModuleDependencyLoader moduleDependencyLoader = new DefaultMemoryModuleDependencyLoader();

    @Override
    public Collection<IModuleWrapper> getModules()
    {
        return Collections.unmodifiableCollection(moduleWrappers);
    }

    @Override
    public Collection<IModuleWrapper> getModules(String group)
    {
        Validate.checkNotNull(group);

        return Iterables.filter(this.getModules(), new Predicate<IModuleWrapper>() {
            @Override
            public boolean test(IModuleWrapper defaultModuleWrapper)
            {
                return defaultModuleWrapper.getModuleConfiguration().group.equals(group);
            }
        });
    }

    @Override
    public IModuleWrapper getModule(String name)
    {
        Validate.checkNotNull(name);

        return Iterables.first(this.moduleWrappers, new Predicate<DefaultModuleWrapper>() {
            @Override
            public boolean test(DefaultModuleWrapper defaultModuleWrapper)
            {
                return defaultModuleWrapper.getModuleConfiguration().getName().equals(name);
            }
        });
    }

    @Override
    public IModuleWrapper loadModule(URL url)
    {
        Validate.checkNotNull(url);

        DefaultModuleWrapper moduleWrapper = null;

        if (Iterables.first(this.moduleWrappers, new Predicate<DefaultModuleWrapper>() {
            @Override
            public boolean test(DefaultModuleWrapper defaultModuleWrapper)
            {
                return defaultModuleWrapper.getUrl().toString().equalsIgnoreCase(url.toString());
            }
        }) != null) return null;

        try
        {

            this.moduleWrappers.add(moduleWrapper = new DefaultModuleWrapper(this, url));
            moduleWrapper.loadModule();

        } catch (Throwable throwable)
        {
            throwable.printStackTrace();

            if (moduleWrapper != null)
                moduleWrapper.unloadModule();
        }

        return moduleWrapper;
    }

    @Override
    public IModuleWrapper loadModule(File file)
    {
        Validate.checkNotNull(file);

        return loadModule(file.toPath());
    }

    @Override
    public IModuleWrapper loadModule(Path path)
    {
        Validate.checkNotNull(path);

        try
        {
            return loadModule(path.toUri().toURL());
        } catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public IModuleProvider loadModule(URL... urls)
    {
        Validate.checkNotNull(urls);

        for (URL url : urls)
            loadModule(url);

        return this;
    }

    @Override
    public IModuleProvider loadModule(File... files)
    {
        Validate.checkNotNull(files);

        for (File file : files)
            loadModule(file);

        return this;
    }

    @Override
    public IModuleProvider loadModule(Path... paths)
    {
        Validate.checkNotNull(paths);

        for (Path path : paths)
            loadModule(path);

        return this;
    }

    @Override
    public IModuleProvider startAll()
    {
        for (DefaultModuleWrapper moduleWrapper : this.moduleWrappers)
            moduleWrapper.startModule();

        return this;
    }

    @Override
    public IModuleProvider stopAll()
    {
        for (DefaultModuleWrapper moduleWrapper : this.moduleWrappers)
            moduleWrapper.stopModule();

        return this;
    }

    @Override
    public IModuleProvider unloadAll()
    {
        for (DefaultModuleWrapper moduleWrapper : this.moduleWrappers)
            moduleWrapper.unloadModule();

        return this;
    }
}