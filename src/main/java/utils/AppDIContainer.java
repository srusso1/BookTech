package utils;

import database.*;
import services.*;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import javafx.util.Callback;

public class AppDIContainer implements Callback<Class<?>, Object> {

    private static AppDIContainer instance;
    private final Map<Class<?>, Object> singletons = new HashMap<>();

    private AppDIContainer() {
        // Register DAOs
        singletons.put(CategoriasDAO.class, new CategoriasDAO());
        singletons.put(DocentesDAO.class, new DocentesDAO());
        singletons.put(EditorialesDAO.class, new EditorialesDAO());
        singletons.put(EstudiantesDAO.class, new EstudiantesDAO());
        singletons.put(InformesDAO.class, new InformesDAO());
        singletons.put(LibrosDAO.class, new LibrosDAO());
        singletons.put(MotivosPlataformaDAO.class, new MotivosPlataformaDAO());
        singletons.put(MotivosPrestamoDAO.class, new MotivosPrestamoDAO());
        singletons.put(PrestamosDAO.class, new PrestamosDAO());
        singletons.put(RegistroPlataformaDAO.class, new RegistroPlataformaDAO());
        singletons.put(UsuariosDAO.class, new UsuariosDAO());
        
        // Register Services
        singletons.put(EstudianteService.class, new EstudianteService());
    }

    public static AppDIContainer getInstance() {
        if (instance == null) {
            instance = new AppDIContainer();
        }
        return instance;
    }

    @Override
    public Object call(Class<?> type) {
        try {
            // First check if we have a registered singleton
            if (singletons.containsKey(type)) {
                return singletons.get(type);
            }

            // Otherwise, try to instantiate the Controller
            for (Constructor<?> constructor : type.getConstructors()) {
                if (constructor.getParameterCount() > 0) {
                    Object[] parameters = new Object[constructor.getParameterCount()];
                    Class<?>[] paramTypes = constructor.getParameterTypes();
                    for (int i = 0; i < paramTypes.length; i++) {
                        parameters[i] = singletons.get(paramTypes[i]); 
                    }
                    return constructor.newInstance(parameters);
                }
            }
            
            // Fallback to default constructor
            return type.getDeclaredConstructor().newInstance();
        } catch (Exception exc) {
            throw new RuntimeException("Error in DI container instantiating " + type.getName(), exc);
        }
    }
}