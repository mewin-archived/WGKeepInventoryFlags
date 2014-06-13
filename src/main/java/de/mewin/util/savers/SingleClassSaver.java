package de.mewin.util.savers;

import de.mewin.util.Saver;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

/**
 *
 * @author mewin<mewin001@hotmail.de>
 */
public abstract class SingleClassSaver<T> extends Saver
{
    @Override
    public Object serialize(Object obj)
    {
        return serializeT((T) obj);
    }
    
    @Override
    public Object deserialize(Object obj)
    {
        return deserializeT(obj);
    }
    
    @Override
    public Class[] getClasses()
    {
        return new Class[] {getParameterClass()};
    }
    
    private Class<?> getParameterClass()
    {
        return (Class<?>) (((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
    }
    
    public abstract Object serializeT(T t);
    
    public abstract T deserializeT(Object obj);
}
