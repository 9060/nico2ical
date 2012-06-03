package org.ryu22e.nico2cal.controller;

import org.slim3.controller.Controller;
import org.slim3.controller.Navigation;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/**
 * ログインするためのコントローラー。
 * @author ryu22e
 *
 */
public final class LoginController extends Controller {

    /*
     * (non-Javadoc) {@inheritDoc}
     */
    @Override
    public Navigation run() throws Exception {
        UserService userService = UserServiceFactory.getUserService();
        return redirect(userService.createLoginURL("/"));
    }
}
