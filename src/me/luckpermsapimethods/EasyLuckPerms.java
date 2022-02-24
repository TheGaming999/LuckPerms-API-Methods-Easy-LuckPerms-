package me.luckpermsapimethods;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.group.GroupManager;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.query.QueryMode;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.track.Track;
import net.luckperms.api.track.TrackManager;

/**
 * 
 * @author TheGaming999
 * <p>Feel free to use it anywhere
 */
public class EasyLuckPerms {

	private LuckPerms luckPerms;
	private UserManager userManager;
	private TrackManager trackManager;
	private GroupManager groupManager;

	public EasyLuckPerms() {
		setupLuckPerms();
		this.userManager = luckPerms.getUserManager();
		this.trackManager = luckPerms.getTrackManager();
		this.groupManager = luckPerms.getGroupManager();
	}

	private void setupLuckPerms() {
		RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
		if (provider != null) {
			luckPerms = provider.getProvider();
		}
	}
	
	public LuckPerms getLuckPerms() {
		return luckPerms;
	}
	
	public UserManager getUserManager() {
		return this.userManager;
	}
	
	public TrackManager getTrackManager() {
		return this.trackManager;
	}
	
	public GroupManager getGroupManager() {
		return this.groupManager;
	}
	
	public User getUser(UUID uniqueId) {
		return userManager.getUser(uniqueId);
	}

	public Track getTrack(String trackName) {
		return trackManager.getTrack(trackName);
	}

	public Group getGroup(String groupName) {
		return groupManager.getGroup(groupName);
	}


	/**
	 * 
	 * @param user user obtained from UserManager
	 * @return all groups that the player inherits / has
	 */
	public Collection<Group> getPlayerGroups(User user) {
		Collection<Group> groups = user.getInheritedGroups(QueryOptions.builder(QueryMode.NON_CONTEXTUAL).build());
		return groups;
	}

	/**
	 * 
	 * @param uniqueId player uuid
	 * @return all groups that the player inherits / has
	 */
	public Collection<Group> getPlayerGroups(UUID uniqueId) {
		return getPlayerGroups(userManager.getUser(uniqueId));
	}

	/**
	 * 
	 * @param name player name
	 * @return all groups that the player inherits / has
	 */
	public Collection<Group> getPlayerGroups(String name) {
		return getPlayerGroups(userManager.getUser(name));
	}

	/**
	 * 
	 * @param uniqueId player uuid
	 * @return player parent group
	 */
	public Group getPlayerGroup(User user) {
		Collection<Group> groups = user.getInheritedGroups(QueryOptions.builder(QueryMode.NON_CONTEXTUAL).build());
		return (groups.toArray(new Group[0])[0]);
	}

	/**
	 * 
	 * @param uniqueId player uuid
	 * @return player parent group
	 */
	public Group getPlayerGroup(UUID uniqueId) {
		return getPlayerGroup(userManager.getUser(uniqueId));
	}

	/**
	 * 
	 * @param name player name
	 * @return player parent group
	 */
	public Group getPlayerGroup(String name) {
		return getPlayerGroup(userManager.getUser(name));
	}

	/**
	 * 
	 * @param user the user that is obtained from UserManager
	 * @param trackName the track to get the groups from
	 * @return groups that the player has on a track
	 */
	public Set<Group> getPlayerGroups(User user, String trackName) {
		Track track = trackManager.getTrack(trackName);
		Set<Group> groups = user.getNodes(NodeType.INHERITANCE).stream()
				.map(InheritanceNode::getGroupName)
				.filter(track::containsGroup)
				.map(groupManager::getGroup)
				.collect(Collectors.toCollection(LinkedHashSet::new));
		return groups;
	}

	/**
	 * 
	 * @param uniqueId player uuid
	 * @param trackName the track to get the groups from
	 * @return player groups on a track
	 */
	public Set<Group> getPlayerGroups(UUID uniqueId, String trackName) {
		return getPlayerGroups(userManager.getUser(uniqueId), trackName);
	}

	/**
	 * 
	 * @param name player name
	 * @param trackName the track to get the groups from
	 * @return player groups on a track
	 */
	public Set<Group> getPlayerGroups(String name, String trackName) {
		return getPlayerGroups(userManager.getUser(name), trackName);
	}

	/**
	 * 
	 * @param groupe the group to search for
	 * @return all tracks that has the specified group
	 */
	public Set<Track> getTracksOfGroup(Group group) {
		Set<Track> tracks = trackManager.getLoadedTracks();
		Set<Track> foundTracks = tracks.stream()
				.filter(track -> track.containsGroup(group))
				.collect(Collectors.toCollection(LinkedHashSet::new));
		return foundTracks;
	}

	/**
	 * 
	 * @param groupName the group name to search for
	 * @return all tracks that has the specified group
	 */
	public Set<Track> getTracksOfGroup(String groupName) {
		return getTracksOfGroup(getGroup(groupName));
	}

	/**
	 * 
	 * @param user the user that is obtained from UserManager
	 * @return all player groups that aren't inserted into tracks
	 */
	public Set<Group> getPlayerTracklessGroups(User user) {
		Set<Group> nodes = user.getNodes(NodeType.INHERITANCE).stream()
				.map(NodeType.INHERITANCE::cast)
				.map(InheritanceNode::getGroupName)
				.filter(groupName -> !isOnTrack(groupName))
				.map(groupManager::getGroup)
				.collect(Collectors.toSet());
		return nodes;
	}

	/**
	 * 
	 * @param uniqueId player uuid
	 * @return all player groups that aren't inserted into tracks
	 */
	public Set<Group> getPlayerTracklessGroups(UUID uniqueId) {
		return getPlayerTracklessGroups(getUser(uniqueId));
	}
	
	/**
	 * 
	 * @param group the group to search for
	 * @param trackName track to search in
	 * @return whether the group is found within a track or not
	 */
	public boolean isOnTrack(Group group, String trackName) {
		Track track = trackManager.getTrack(trackName);
		boolean findTrack = track.containsGroup(group);
		return findTrack;
	}
	
	/**
	 * 
	 * @param groupName the group name to search for
	 * @param trackName track to search in
	 * @return whether the group is found within a track or not
	 */
	public boolean isOnTrack(String groupName, String trackName) {
		Track track = trackManager.getTrack(trackName);
		boolean findTrack = track.containsGroup(groupName);
		return findTrack;
	}
	
	/**
	 * 
	 * @param InheritanceNode the group node to search for
	 * @param trackName track to search in
	 * @return whether the group is found within a track or not
	 */
	public boolean isOnTrack(InheritanceNode inheritanceNode, String trackName) {
		Track track = trackManager.getTrack(trackName);
		boolean findTrack = track.containsGroup(inheritanceNode.getGroupName());
		return findTrack;
	}
	
	/**
	 * 
	 * @param group the group to search for
	 * @return whether the group is found within a track or not
	 */
	public boolean isOnTrack(Group group) {
		Set<Track> tracks = trackManager.getLoadedTracks();
		boolean findTrack = tracks.stream().anyMatch(track -> track.containsGroup(group));
		return findTrack;
	}

	/**
	 * 
	 * @param groupName the group name to search for
	 * @return whether the group is found within a track or not
	 */
	public boolean isOnTrack(String groupName) {
		Set<Track> tracks = trackManager.getLoadedTracks();
		boolean findTrack = tracks.stream().anyMatch(track -> track.containsGroup(groupName));
		return findTrack;
	}

	/**
	 * 
	 * @param inheritanceNode the group node to search for
	 * @return whether the group is found within a track or not
	 */
	public boolean isOnTrack(InheritanceNode inheritanceNode) {
		Set<Track> tracks = trackManager.getLoadedTracks();
		boolean findTrack = tracks.stream().anyMatch(track -> track.containsGroup(inheritanceNode.getGroupName()));
		return findTrack;
	}

	/**
	 * @author Evan#6000
	 * @param user the user that is obtained from UserManager
	 * @param trackName track name to get the group from
	 * @return the group with the highest weight that the player has, on a track
	 */
	public Optional<Group> getPlayerHighestGroupOnTrack(User user, String trackName) {
		Track track = getTrack(trackName);
		SortedSet<Node> allNodes = user.getDistinctNodes();
		if (!allNodes.isEmpty()) {
			return allNodes.stream()
					.filter(NodeType.INHERITANCE::matches)
					.map(NodeType.INHERITANCE::cast)
					.map(InheritanceNode::getGroupName)
					.map(groupManager::getGroup)
					.filter(Objects::nonNull)
					.filter(track::containsGroup)
					.max(Comparator.comparingInt(g -> g.getWeight().orElse(0)));
		}
		return Optional.empty();
	}

	/**
	 * @author Evan#6000
	 * @param uniqueId player uuid
	 * @param trackName track name to get the group from
	 * @return the group with the highest weight that the player has, on a track
	 */
	public Optional<Group> getPlayerHighestGroupOnTrack(UUID uniqueId, String trackName) {
		return getPlayerHighestGroupOnTrack(getUser(uniqueId), trackName);
	}

	/**
	 * Clears all the previous groups and then changes player parent group to the specified group.
	 * @param uniqueId player uuid
	 * @param group the desired group
	 */
	public CompletableFuture<Void> setPlayerGroup(UUID uniqueId, Group group) {
		return userManager.modifyUser(uniqueId, (User user) -> {
			user.data().clear(NodeType.INHERITANCE::matches);
			Node node = InheritanceNode.builder(group).build();
			user.data().add(node);
		});
	}

	/**
	 * Clears all of the player previous groups and then changes player parent group to the specified group.
	 * @param uniqueId player uuid
	 * @param group the desired group
	 * @param ignoreTracks if true, ignores clearing groups that are found within tracks i.e clears all groups but groups within tracks
	 */
	public CompletableFuture<Void> setPlayerGroup(UUID uniqueId, Group group, boolean ignoreTracks) {
		if(!ignoreTracks) return setPlayerGroup(uniqueId, group);
		return userManager.modifyUser(uniqueId, (User user) -> {
			user.data().clear(NodeType.INHERITANCE.predicate(trackless -> !isOnTrack(trackless.getGroupName())));
			Node node = InheritanceNode.builder(group).build();
			user.data().add(node);
		});
	}

	/**
	 * Changes player group to the desired group on a specfic track taken from trackName 
	 * after removing past group nodes that is included in the specified track.
	 * @param uniqueId player uuid
	 * @param group the desired group
	 * @param trackName track to change player group on
	 */
	public CompletableFuture<Void> setPlayerGroup(UUID uniqueId, Group group, String trackName) {
		return userManager.modifyUser(uniqueId, (User user) -> {
			user.data().clear(NodeType.INHERITANCE.predicate(foundNode -> isOnTrack(foundNode, trackName)));
			Node node = InheritanceNode.builder(group).build();
			user.data().add(node);
		});
	}
	
	/**
	 * 
	 * @param uniqueId player uuid
	 * @param trackName the track to remove player group from
	 */
	public CompletableFuture<Void> deletePlayerGroups(UUID uniqueId, String trackName) {
    	Track track = getTrack(trackName);
    	return userManager.modifyUser(uniqueId, (User user) -> {
    		user.data().clear(NodeType.INHERITANCE.predicate(node -> track.containsGroup(node.getGroupName())));
    	});
    }
	
	/**
	 * @param uniqueId player uuid
	 */
	public CompletableFuture<Void> deletePlayerGroups(UUID uniqueId) {
    	return userManager.modifyUser(uniqueId, (User user) -> {
    		user.data().clear(NodeType.INHERITANCE::matches);
    	});
    }

	/**
	 * @param uniqueId player uuid
	 * @param ignoreTracks ignores clearing player groups that are found within tracks
	 */
	public CompletableFuture<Void> deletePlayerGroups(UUID uniqueId, boolean ignoreTracks) {
    	return userManager.modifyUser(uniqueId, (User user) -> {
    		user.data().clear(NodeType.INHERITANCE.predicate(trackless -> !isOnTrack(trackless.getGroupName())));
    	});
    }
	
}
