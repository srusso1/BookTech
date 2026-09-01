package controllers.Login;

import database.PrestamosDAO;
import database.UsuariosDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import model.Usuario;
import utils.Alertas;
import utils.ManagerView;
import utils.Paths;
import utils.Validaciones;

public class LoginController {

    public LoginController(UsuariosDAO usuariosDAO, PrestamosDAO prestamosDAO) {
        this.usuariosDAO = usuariosDAO;
        this.prestamosDAO = prestamosDAO;
    }

    @FXML
    private Button btnIngresar;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private AnchorPane contenedor;

    @FXML
    private TextField txtUsuario;

    private final UsuariosDAO usuariosDAO;
    private final PrestamosDAO prestamosDAO;

    @FXML
    void clickIngresar(ActionEvent event) {
        ingresar();
    }

    private void ingresar(){
        if(!Validaciones.campoRequerido(txtUsuario)){
            return;
        }

        if(!Validaciones.validarUsuario(txtUsuario)){
            return;
        }

        if(!Validaciones.campoRequerido(txtPassword)){
            return;
        }

        if(!Validaciones.validarPassword(txtPassword)){
            return;
        }

        String usuario = txtUsuario.getText();
        String password = txtPassword.getText();

        Usuario user = usuariosDAO.validarUsuario(usuario, password);
        if(user == null){
            Alertas.mostrarError("ERROR: Usuario no encontrado o credenciales incorrectas");
            return;
        }

        Alertas.mostrarExito("Bienvenido " + user.getClass().getSimpleName().toUpperCase() + " " + user.getNombreCompleto());
        
        // Guardar sesión global
        utils.SessionManager.getInstance().setUsuarioActual(user);

        if(user.getRol() == model.enums.RolUsuario.BIBLIOTECARIO.getId()){
            ManagerView.cargarVista(contenedor, Paths.DASHBOARD_BIBLIOTECARIO);
            int vencidos = prestamosDAO.actualizarPrestamosTarde();
            if(vencidos > 0) {
                Alertas.mostrarInfo("Se actualizaron " + vencidos + " préstamos vencidos. Consulte el módulo 'Préstamos activos'.");
            }
        }else if(user.getRol() == model.enums.RolUsuario.RECTOR.getId()){
            ManagerView.cargarVista(contenedor, Paths.DASHBOARD_RECTORIA);
        }


    }

    @FXML
    void initialize(){

    }


}