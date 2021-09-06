package cn.gzsendi.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.protocol.GuacamoleClientInformation;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.apache.guacamole.websocket.GuacamoleWebSocketTunnelEndpoint;
import org.springframework.stereotype.Component;

/**
 * @author pancm
 * @Title: WebSocketTunnel
 * @Description:
 * @Version:1.0.0
 * @Since:jdk1.8
 * @Date 2021/8/2
 **/
@ServerEndpoint(value = "/webSocket", subprotocols = "guacamole")
@Component
public class WebSocketTunnel extends GuacamoleWebSocketTunnelEndpoint {


    private MyConfig myConfig = (MyConfig)SpringBeanFactory.getBean("myConfig");


    /**
     * Returns a new tunnel for the given session. How this tunnel is created
     * or retrieved is implementation-dependent.
     *
     * @param session        The session associated with the active WebSocket
     *                       connection.
     * @param endpointConfig information associated with the instance of
     *                       the endpoint created for handling this single connection.
     * @return A connected tunnel, or null if no such tunnel exists.
     * @throws GuacamoleException If an error occurs while retrieving the
     *                            tunnel, or if access to the tunnel is denied.
     */
    @Override
    protected GuacamoleTunnel createTunnel(Session session, EndpointConfig endpointConfig) throws GuacamoleException {
        System.out.println("sessionMap:" + session.getRequestParameterMap());
        System.out.println("myConfig:" + myConfig.getPort());
        // 获取url的值
        Integer height = Integer.valueOf(session.getRequestParameterMap().get("height").get(0));
        Integer width = Integer.valueOf(session.getRequestParameterMap().get("width").get(0));
        GuacamoleClientInformation information = new GuacamoleClientInformation();
        information.setOptimalScreenHeight(height);
        information.setOptimalScreenWidth(width);
        //guacamole server地址 r端口
        String hostname = "127.0.0.1";
        int port = 4822;
        
        //--------------------------ssh连接测试--------------------------
       /* GuacamoleConfiguration configuration = new GuacamoleConfiguration();
        configuration.setProtocol("ssh");
        // 远程ssh服务的地址
        configuration.setParameter("hostname", "192.168.56.101");
        configuration.setParameter("port", "22");
        configuration.setParameter("username", "testuser");
        configuration.setParameter("password", "123456");*/
        //--------------------------ssh连接测试--------------------------

        //--------------------------windows远程桌面测试--------------------------
        GuacamoleConfiguration configuration = new GuacamoleConfiguration();
        configuration.setProtocol("rdp");
        // 远程windows服务的地址
        configuration.setParameter("hostname", "172.168.201.11");
        configuration.setParameter("port", "3389");
        configuration.setParameter("username", "testuser");
        configuration.setParameter("password", "11234");
        configuration.setParameter("ignore-cert", "true");


		//添加会话录制--录屏,以下5行代码好像没有作用。但远程桌面还是没有问题的。
        /*String fileName = getNowTime() + ".guac";//文件名
        String outputFilePath = "d:/temp";
        configuration.setParameter("recording-path", outputFilePath);
        configuration.setParameter("create-recording-path", "true");
        configuration.setParameter("recording-name", fileName);*/
      //--------------------------windows远程桌面测试--------------------------

        GuacamoleSocket socket = new ConfiguredGuacamoleSocket(
                new InetGuacamoleSocket(hostname, port),
                configuration,
                information
        );

        GuacamoleTunnel tunnel = new SimpleGuacamoleTunnel(socket);
        return tunnel;
    }

    private void optClose(Session session) {
        // 判断当前连接是否还在线
        if (session.isOpen()) {
            try {
                // 关闭连接
                CloseReason closeReason = new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "进行关闭！");
                session.close(closeReason);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private String getNowTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}
