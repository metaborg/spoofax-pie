package mb.tiger;

import mb.resource.classloader.ClassLoaderToNativeResolver;
import mb.resource.classloader.ClassLoaderUrlResolver;
import mb.resource.common.ClassLoaderResources;

public class TigerClassloaderResources extends ClassLoaderResources {
    public TigerClassloaderResources(ClassLoaderUrlResolver urlResolver, ClassLoaderToNativeResolver toNativeResolver) {
        super("mb-tiger-classloader-resource", TigerClassloaderResources.class.getClassLoader(), urlResolver, toNativeResolver, "mb/tiger");
    }
}
