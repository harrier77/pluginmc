package myfakep;

import java.util.Iterator;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import net.minecraft.server.v1_12_R1.PlayerInteractManager;
import net.minecraft.server.v1_12_R1.WorldServer;

public class Main extends JavaPlugin {
	private static Main instance;
	private ProtocolManager protocolManager;
	public NPCManager npcManager;
	
	public static Main getInstance() {
        return instance;
    }
	
	private void setInstance(Main instance) {
        Main.instance = instance;
    }
	@Override
    public void onEnable() {
		Logger log = Bukkit.getLogger();
		log.info("Queston plugin è enabled 1");
		setInstance(this);
        this.getCommand("fp").setExecutor(new FPCommand());
        this.npcManager = new NPCManager();
    }
   
	
	
	@SuppressWarnings("deprecation")
	public void players_number(String numero) {
		 protocolManager = ProtocolLibrary.getProtocolManager();
		 
		 protocolManager.addPacketListener(
		  new PacketAdapter(PacketAdapter.params(this, PacketType.Status.Server.OUT_SERVER_INFO).optionAsync()) {
		    @Override
		    public void onPacketSending(PacketEvent event) {
		        WrappedServerPing ping = event.getPacket().getServerPings().read(0);
		        //ping.setPlayersVisible(false);
		        ping.setPlayersOnline(Integer.parseInt(numero));
		    }
		  });
	 
	 } //EOF class 
	
	
	@Override
    public void onDisable() {
       
    }

}

class NPCManager {

    public void createNPC(Player player, String npcName) {
        Location location = player.getLocation();
        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer nmsWorld = ((CraftWorld) player.getWorld()).getHandle();
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "" + npcName);

        EntityPlayer npc = new EntityPlayer(nmsServer, nmsWorld, gameProfile, new PlayerInteractManager(nmsWorld));
        Player npcPlayer = npc.getBukkitEntity().getPlayer();
        npcPlayer.setPlayerListName(npcName);

        npc.setLocation(location.getX()+1, location.getY(), location.getZ()+1, player.getLocation().getYaw(), player.getLocation().getPitch());
        
        //la connessione viene attivata usando net.minecraft.server che non è documentata?
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc));
        //connection.sendPacket(new PacketPlayOutNamedEntitySpawn(npc));
        //connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, npc));
        player.sendMessage("Fakeplayer "+npcName+" creato...");

    }
}

class FPCommand implements CommandExecutor {

    private Main plugin = Main.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;
            System.err.println("comando");
            if (command.getName().equalsIgnoreCase("fp")) {
            	System.err.println("comando fp");
            	if (args.length == 0) { 
            		player.sendMessage("[nome o numero]");
            	}
            	if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("create")) {
                        String npcName = args[1];
                        plugin.npcManager.createNPC(player, npcName);
                    }
                    if (args[0].equalsIgnoreCase("number")) {
                    	player.sendMessage("numero settato");
                    	String numero = args[1];
                    	plugin.players_number(numero);
                    }
                }
            }
        }
        return true;
    }
}