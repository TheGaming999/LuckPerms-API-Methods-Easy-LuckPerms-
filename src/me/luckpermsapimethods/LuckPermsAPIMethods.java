package me.luckpermsapimethods;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.luckperms.api.model.group.Group;
import net.luckperms.api.track.Track;

public class LuckPermsAPIMethods extends JavaPlugin {

	private EasyLuckPerms easyLuckPerms;
	private final String argumentsExplanationMessage = colorize("&a<required> &7&kii&r &e[optional]");
	private final String setGroupUsageMessage = colorize("&7/lpam setgroup &a<player> <group> &e[track]");
	private final String getGroupUsageMessage = colorize("&7/lpam getgroup &a<player> &e[track]");
	private final String getGroupsUsageMessage = colorize("&7/lpam getgroups &a<player>");
	private final String getTrackGroupsUsageMessage = colorize("&7/lpam gettrackgroups");
	private final String unknownSubCommandMessage = colorize("&cUnknown subcommand!");

	public void onEnable() {
		this.easyLuckPerms = new EasyLuckPerms();
		this.getLogger().info("Enabled.");
	}

	public void onDisable() {
		this.getLogger().info("Disabled.");
	}
	
	private String colorize(String textToTranslate) {
		return ChatColor.translateAlternateColorCodes('&', textToTranslate);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getLabel().equalsIgnoreCase("lpam")) {
			switch(args.length) {
			case 0:
				sender.sendMessage(argumentsExplanationMessage);
				sender.sendMessage(setGroupUsageMessage);
				sender.sendMessage(getGroupUsageMessage);
				sender.sendMessage(getGroupsUsageMessage);
				sender.sendMessage(getTrackGroupsUsageMessage);
				break;
			case 1:
				switch (args[0].toLowerCase()) {
				case "setgroup":
					sender.sendMessage(setGroupUsageMessage);
					break;
				case "getgroup":
					sender.sendMessage(getGroupUsageMessage);
					break;
				case "getgroups":
					sender.sendMessage(getGroupsUsageMessage);
					break;
				case "gettrackgroups":
					Set<Track> tracks = easyLuckPerms.getTrackManager().getLoadedTracks();
					tracks.forEach(track -> {
						sender.sendMessage("Track: " + track.getName() + " -> Groups: " + 
								StringUtils.join(track.getGroups(), ", ") + ".");
					});
					break;
				default:
					sender.sendMessage(unknownSubCommandMessage);
					break;
				}
				break;
			case 2:
				switch (args[0].toLowerCase()) {
				case "setgroup":
					sender.sendMessage(setGroupUsageMessage);
					break;
				case "getgroup":
					Player player = Bukkit.getPlayer(args[1]);
					if(player == null) {
						sender.sendMessage("'" + args[1] + "' is not online!");
						return true;
					}	
					Group group = easyLuckPerms.getPlayerGroup(player.getUniqueId());
					String groupName = group.getName();
					sender.sendMessage("'" + player.getName() + "' parent group is '" + groupName + "'");
					break;
				case "getgroups":
					Player player2 = Bukkit.getPlayer(args[1]);
					if(player2 == null) {
						sender.sendMessage("'" + args[1] + "' is not online!");
						return true;
					}	
					Set<Group> tracklessGroups = easyLuckPerms.getPlayerTracklessGroups(player2.getUniqueId());
					sender.sendMessage("Trackless groups: " + StringUtils.join(tracklessGroups.stream().map(Group::getName).collect(Collectors.toList()), ", ") + ". \n");
					sender.sendMessage("Groups with tracks: ");
					easyLuckPerms.getTrackManager().getLoadedTracks().forEach(track -> {
						String trackName = track.getName();
						Set<Group> trackGroups = easyLuckPerms.getPlayerGroups(player2.getUniqueId(), trackName);
						sender.sendMessage(trackName + " -> " + StringUtils.join(trackGroups.stream().map(Group::getName).collect(Collectors.toList()), ", ") + ".");
					});
					break;
				default:
					sender.sendMessage(unknownSubCommandMessage);
					break;
				}
				break;
			case 3:
				switch (args[0].toLowerCase()) {
				case "setgroup":
					Player player = Bukkit.getPlayer(args[1]);
					if(player == null) {
						sender.sendMessage("'" + args[1] + "' is not online!");
						return true;
					}
					Group group = easyLuckPerms.getGroup(args[2]);
					if(group == null) {
						sender.sendMessage("The group '" + args[2] + "' doesn't exist!");
						return true;
					}
					if(easyLuckPerms.isOnTrack(group)) {
						sender.sendMessage("The group '" + group.getName() + "' is found within a track! Use the [track] argument.");
						return true;
					}
					easyLuckPerms.setPlayerGroup(player.getUniqueId(), group, true);
					easyLuckPerms.getLuckPerms().getUserManager().getUser(player.getUniqueId()).setPrimaryGroup(group.getName());
					sender.sendMessage("Changed parent group of '" + player.getName() + "' to '" + group.getName() + "'");
					break;
				case "getgroup":
					Player player2 = Bukkit.getPlayer(args[1]);
					if(player2 == null) {
						sender.sendMessage("'" + args[1] + "' is not online!");
						return true;
					}	
					Track track = easyLuckPerms.getTrack(args[2]);
					if(track == null) {
						sender.sendMessage("The track '" + args[2] + "' doesn't exist!");
						return true;
					}
					Optional<Group> group2 = easyLuckPerms.getPlayerHighestGroupOnTrack(player2.getUniqueId(), args[2]);
					if(!group2.isPresent()) {
						sender.sendMessage("The player '" + player2.getName() + "' doesn't have a group on the track '" + args[2] + "'");
						return true;
					}
					sender.sendMessage("'" + player2.getName() + "' group on track '" + track.getName() + "' is '" + group2.get().getName() + "'");

					break;
				default:
					sender.sendMessage(unknownSubCommandMessage);
					break;
				}
				break;
			case 4:
				switch (args[0].toLowerCase()) {
				case "setgroup":
					Player player = Bukkit.getPlayer(args[1]);
					if(player == null) {
						sender.sendMessage("'" + args[1] + "' is not online!");
						return true;
					}
					Group group = easyLuckPerms.getGroup(args[2]);
					if(group == null) {
						sender.sendMessage("The group '" + args[2] + "' doesn't exist!");
						return true;
					}
					Track track = easyLuckPerms.getTrack(args[3]);
					if(track == null) {
						sender.sendMessage("The track '" + args[3] + "' doesn't exist!");
						return true;
					}
					if(!easyLuckPerms.isOnTrack(group, track.getName())) {
						sender.sendMessage("The group '" + group.getName() + "' is not on the track '" + track.getName() + "'");
						return true;
					}
					easyLuckPerms.setPlayerGroup(player.getUniqueId(), group, track.getName());
					sender.sendMessage("Changed group of '" + player.getName() + "' on track '" + track.getName() + "' to '" + group.getName() + "'");
					break;
				default:
					sender.sendMessage(unknownSubCommandMessage);
					break;
				}
				break;
			default:

				break;
			}

		}
		return true;
	}

}
