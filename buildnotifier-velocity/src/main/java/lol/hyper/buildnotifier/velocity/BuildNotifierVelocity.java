/*
 * This file is part of BuildNotifier.
 *
 * BuildNotifier is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BuildNotifier is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BuildNotifier.  If not, see <https://www.gnu.org/licenses/>.
 */

package lol.hyper.buildnotifier.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import lol.hyper.buildnotifier.core.VelocityPlugin;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Plugin(
        id = "buildnotifiervelocity",
        name = "BuildNotifier-Velocity",
        version = "1.0",
        authors = {"hyperdefined"},
        description = "Automatically check for Paper/Waterfall/Velocity updates.",
        url = "https://github.com/hyperdefined/BuildNotifier"
)
public final class BuildNotifierVelocity {

    private final Logger logger;
    public final ProxyServer server;

    public VelocityPlugin velocityPlugin;
    public int buildNumber = -1;

    @Inject
    public BuildNotifierVelocity(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // get some basic information about the server
        String version = server.getVersion().getVersion();
        String[] versionParts = version.split(" ", 2);
        String velocityVersion = versionParts[0];
        // use regex to get the build
        String patternString = "b\\d+";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(versionParts[1]);
        if (matcher.find()) {
            buildNumber = Integer.parseInt(matcher.group().replace("b", ""));
        }

        // if the regex failed, don't bother checking
        if (buildNumber == -1 || velocityVersion == null) {
            return;
        }

        logger.info("Running Velocity version: " + velocityVersion);
        logger.info("Running Velocity build: " + buildNumber);
        logger.info("Supporting Minecraft versions: " + ProtocolVersion.SUPPORTED_VERSION_STRING);

        velocityPlugin = new VelocityPlugin(logger, velocityVersion, buildNumber);
        int latestVelocityBuild = velocityPlugin.getLatestBuild();
        // Server is outdated
        if (buildNumber < latestVelocityBuild) {
            logger.warning("Your Velocity version is outdated. The latest build is " + latestVelocityBuild + ".");
            logger.warning("You are currently " + velocityPlugin.getBuildsBehind() + " build(s) behind.");
        }
    }
}
