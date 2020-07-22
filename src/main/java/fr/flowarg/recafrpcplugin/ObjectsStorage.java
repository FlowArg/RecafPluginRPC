package fr.flowarg.recafrpcplugin;

import javax.annotation.Nullable;

public class ObjectsStorage<T, U>
{
    @Nullable
    private final T firstObject;
    @Nullable
    private final U secondObject;

    public ObjectsStorage(@Nullable T firstObject, @Nullable U secondObject)
    {
        this.firstObject = firstObject;
        this.secondObject = secondObject;
    }

    @Nullable
    public T getFirstObject()
    {
        return this.firstObject;
    }

    @Nullable
    public U getSecondObject()
    {
        return this.secondObject;
    }
}
