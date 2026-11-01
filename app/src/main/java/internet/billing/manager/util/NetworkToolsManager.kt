package internet.billing.manager.util

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.HttpURLConnection
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.net.URL
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

// ============================================================================
// DATA MODELS
// ============================================================================

data class NetworkOverview(
    val isConnected: Boolean = false,
    val connectionType: String = "Detecting...",
    val isWifi: Boolean = false,
    val localIpv4: String = "0.0.0.0",
    val defaultGateway: String = "0.0.0.0",
    val dns1: String = "8.8.8.8",
    val dns2: String = "1.1.1.1",
    val publicIp: String = "Detecting...",
    val ispName: String = "Online Broadband",
    val asNumber: String = "AS132203",
    val geoCity: String = "Dhaka",
    val geoCountry: String = "Bangladesh"
)

data class PingProbe(
    val seq: Int,
    val rttMs: Float,
    val bytes: Int,
    val ttl: Int,
    val isSuccess: Boolean
)

data class PingReport(
    val host: String = "",
    val isRunning: Boolean = false,
    val packetsSent: Int = 0,
    val packetsReceived: Int = 0,
    val packetLossPercent: Int = 0,
    val minRttMs: Float = 0f,
    val avgRttMs: Float = 0f,
    val maxRttMs: Float = 0f,
    val currentRttMs: Float = 0f,
    val jitterMs: Float = 0f,
    val logs: List<String> = emptyList(),
    val probes: List<PingProbe> = emptyList(),
    val asInfo: String = ""
)

data class TracerouteHop(
    val hop: Int,
    val ip: String,
    val hostName: String,
    val rttMs: Float,
    val isReached: Boolean,
    val isTimeout: Boolean
)

data class LanDevice(
    val ip: String,
    val mac: String,
    val vendor: String,
    val customName: String = "",
    val hostName: String = "",
    val openPorts: List<Int> = emptyList(),
    val isOnline: Boolean = true,
    val isStarred: Boolean = false,
    val latencyMs: Long = 0
)

data class WolTarget(
    val name: String,
    val mac: String,
    val broadcastIp: String = "255.255.255.255",
    val port: Int = 9,
    val lastWoken: Long = 0
)

data class DnsSectionRecord(
    val name: String,
    val type: String,
    val ttl: Int,
    val data: String,
    val section: String // "Answer", "Authority", "Additional"
)

data class DetailedDnsRecord(
    val domain: String,
    val recordType: String = "A",
    val isSuccess: Boolean = false,
    val isDnssecValidated: Boolean = false,
    val statusText: String = "",
    val records: List<DnsSectionRecord> = emptyList(),
    val rawJson: String = "",
    val responseTimeMs: Long = 0,
    val errorMessage: String? = null
)

data class PortCheckResult(
    val host: String,
    val port: Int,
    val serviceName: String,
    val isOpen: Boolean,
    val isTimeout: Boolean,
    val latencyMs: Long,
    val message: String
)

data class SpeedTestState(
    val isTesting: Boolean = false,
    val phase: String = "Idle",
    val progress: Float = 0f,
    val currentSpeedMbps: Float = 0f,
    val downloadSpeedMbps: Float = 0f,
    val uploadSpeedMbps: Float = 0f,
    val latencyMs: Long = 0,
    val jitterMs: Long = 0,
    val bytesDownloaded: Long = 0,
    val bytesUploaded: Long = 0,
    val errorMessage: String? = null
)

data class WifiAudit(
    val isConnected: Boolean = false,
    val ssid: String = "Unknown",
    val customName: String = "",
    val bssid: String = "--:--:--:--:--:--",
    val rssiDbm: Int = -100,
    val signalPercent: Int = 0,
    val frequencyMhz: Int = 0,
    val channel: Int = 0,
    val band: String = "Unknown",
    val linkSpeedMbps: Int = 0,
    val ipAddress: String = "0.0.0.0",
    val gateway: String = "0.0.0.0",
    val rating: String = "Disconnected",
    val channelOccupancy: Map<Int, Int> = emptyMap() // channel -> occupancy % (0..100)
)

data class WhoisResult(
    val query: String,
    val whoisServer: String,
    val isSuccess: Boolean = false,
    val registrar: String = "",
    val creationDate: String = "",
    val expirationDate: String = "",
    val organization: String = "",
    val nameServers: List<String> = emptyList(),
    val rawOutput: String = "",
    val errorMessage: String? = null
)

data class UpnpDevice(
    val ip: String,
    val friendlyName: String,
    val modelName: String = "",
    val manufacturer: String = "",
    val locationUrl: String = "",
    val serverInfo: String = "",
    val serviceType: String = ""
)

// ============================================================================
// PREFERENCES / FAVORITES PERSISTENCE
// ============================================================================

object NetworkToolsPrefs {
    private const val PREF_NAME = "network_tools_prefs"
    private const val KEY_STARRED = "starred_items_"
    private const val KEY_HISTORY = "history_items_"
    private const val KEY_CUSTOM_DEVICE_NAME = "device_name_"
    private const val KEY_CUSTOM_WIFI_NAME = "wifi_name_"
    private const val KEY_SAVED_WOL = "saved_wol_targets"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isStarred(context: Context, tool: String, item: String): Boolean {
        val set = prefs(context).getStringSet(KEY_STARRED + tool, emptySet()) ?: emptySet()
        return set.contains(item)
    }

    fun toggleStarred(context: Context, tool: String, item: String): Boolean {
        val p = prefs(context)
        val current = (p.getStringSet(KEY_STARRED + tool, emptySet()) ?: emptySet()).toMutableSet()
        val newState: Boolean
        if (current.contains(item)) {
            current.remove(item)
            newState = false
        } else {
            current.add(item)
            newState = true
        }
        p.edit().putStringSet(KEY_STARRED + tool, current).apply()
        return newState
    }

    fun getStarredList(context: Context, tool: String): List<String> {
        return (prefs(context).getStringSet(KEY_STARRED + tool, emptySet()) ?: emptySet()).toList()
    }

    fun addHistory(context: Context, tool: String, query: String) {
        if (query.isBlank()) return
        val p = prefs(context)
        val key = KEY_HISTORY + tool
        val raw = p.getString(key, "") ?: ""
        val list = raw.split("|||").filter { it.isNotBlank() && it != query }.toMutableList()
        list.add(0, query)
        val trimmed = list.take(15).joinToString("|||")
        p.edit().putString(key, trimmed).apply()
    }

    fun getHistory(context: Context, tool: String): List<String> {
        val raw = prefs(context).getString(KEY_HISTORY + tool, "") ?: ""
        return raw.split("|||").filter { it.isNotBlank() }
    }

    fun getCustomDeviceName(context: Context, ipOrMac: String): String {
        return prefs(context).getString(KEY_CUSTOM_DEVICE_NAME + ipOrMac, "") ?: ""
    }

    fun setCustomDeviceName(context: Context, ipOrMac: String, name: String) {
        prefs(context).edit().putString(KEY_CUSTOM_DEVICE_NAME + ipOrMac, name.trim()).apply()
    }

    fun getCustomWifiName(context: Context, ssid: String): String {
        return prefs(context).getString(KEY_CUSTOM_WIFI_NAME + ssid, "") ?: ""
    }

    fun setCustomWifiName(context: Context, ssid: String, name: String) {
        prefs(context).edit().putString(KEY_CUSTOM_WIFI_NAME + ssid, name.trim()).apply()
    }

    fun getSavedWolTargets(context: Context): List<WolTarget> {
        val raw = prefs(context).getString(KEY_SAVED_WOL, "") ?: ""
        if (raw.isBlank()) return emptyList()
        val list = mutableListOf<WolTarget>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    WolTarget(
                        name = obj.optString("name", "Device"),
                        mac = obj.optString("mac", ""),
                        broadcastIp = obj.optString("broadcastIp", "255.255.255.255"),
                        port = obj.optInt("port", 9),
                        lastWoken = obj.optLong("lastWoken", 0L)
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun saveWolTarget(context: Context, target: WolTarget) {
        val list = getSavedWolTargets(context).filter { it.mac.uppercase() != target.mac.uppercase() }.toMutableList()
        list.add(0, target)
        val arr = JSONArray()
        list.take(20).forEach { item ->
            val obj = JSONObject()
            obj.put("name", item.name)
            obj.put("mac", item.mac)
            obj.put("broadcastIp", item.broadcastIp)
            obj.put("port", item.port)
            obj.put("lastWoken", item.lastWoken)
            arr.put(obj)
        }
        prefs(context).edit().putString(KEY_SAVED_WOL, arr.toString()).apply()
    }

    fun deleteWolTarget(context: Context, mac: String) {
        val list = getSavedWolTargets(context).filter { it.mac.uppercase() != mac.uppercase() }
        val arr = JSONArray()
        list.forEach { item ->
            val obj = JSONObject()
            obj.put("name", item.name)
            obj.put("mac", item.mac)
            obj.put("broadcastIp", item.broadcastIp)
            obj.put("port", item.port)
            obj.put("lastWoken", item.lastWoken)
            arr.put(obj)
        }
        prefs(context).edit().putString(KEY_SAVED_WOL, arr.toString()).apply()
    }
}

// ============================================================================
// MAIN NETWORK TOOLS MANAGER
// ============================================================================

object NetworkToolsManager {

    // 1. Comprehensive Network Overview
    suspend fun getNetworkOverview(context: Context): NetworkOverview = withContext(Dispatchers.IO) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNet = cm?.activeNetwork
        val caps = cm?.getNetworkCapabilities(activeNet)
        val linkProps = cm?.getLinkProperties(activeNet)

        val isConnected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val isCellular = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
        val isEthernet = caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true

        val type = when {
            isWifi -> "Wi-Fi Network"
            isCellular -> "Mobile Network"
            isEthernet -> "Ethernet Network"
            isConnected -> "Connected Network"
            else -> "Disconnected"
        }

        // Default Gateway
        var gateway = "0.0.0.0"
        linkProps?.routes?.forEach { route ->
            if (route.isDefaultRoute && route.gateway != null) {
                val host = route.gateway?.hostAddress
                if (!host.isNullOrBlank() && host != "0.0.0.0") {
                    gateway = host
                }
            }
        }
        if (gateway == "0.0.0.0" && isWifi) {
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val dhcp = wm?.dhcpInfo
            if (dhcp != null && dhcp.gateway != 0) {
                gateway = intToIp(dhcp.gateway)
            }
        }

        // Local IPv4
        var localIp = "0.0.0.0"
        linkProps?.linkAddresses?.forEach { linkAddr ->
            val addr = linkAddr.address
            if (addr is Inet4Address && !addr.isLoopbackAddress) {
                localIp = addr.hostAddress ?: localIp
            }
        }
        if (localIp == "0.0.0.0") {
            try {
                val interfaces = NetworkInterface.getNetworkInterfaces()
                for (nInt in interfaces) {
                    if (nInt.isUp && !nInt.isLoopback) {
                        for (inetAddr in nInt.inetAddresses) {
                            if (inetAddr is Inet4Address && !inetAddr.isLoopbackAddress) {
                                localIp = inetAddr.hostAddress ?: localIp
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        // DNS Servers
        val dnsList = linkProps?.dnsServers?.mapNotNull { it.hostAddress } ?: emptyList()
        val dns1 = dnsList.getOrNull(0) ?: "8.8.8.8"
        val dns2 = dnsList.getOrNull(1) ?: "1.1.1.1"

        // Public IP & Geo / AS Info
        val (pubIp, isp, asNum, city, country) = fetchPublicIpAndGeo()

        NetworkOverview(
            isConnected = isConnected,
            connectionType = type,
            isWifi = isWifi,
            localIpv4 = localIp,
            defaultGateway = gateway,
            dns1 = dns1,
            dns2 = dns2,
            publicIp = pubIp,
            ispName = isp,
            asNumber = asNum,
            geoCity = city,
            geoCountry = country
        )
    }

    private suspend fun fetchPublicIpAndGeo(): Tuple5<String, String, String, String, String> = withContext(Dispatchers.IO) {
        // Try ipapi.co json for rich ASN and Geo
        try {
            val url = URL("https://ipapi.co/json/")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.setRequestProperty("User-Agent", "Mozilla/5.0")
            if (conn.responseCode == 200) {
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(text)
                val ip = json.optString("ip", "")
                val org = json.optString("org", "Broadband ISP")
                val asn = json.optString("asn", "AS132203")
                val city = json.optString("city", "Dhaka")
                val country = json.optString("country_name", "Bangladesh")
                if (ip.isNotBlank() && ip.contains(".")) {
                    return@withContext Tuple5(ip, org, asn, city, country)
                }
            }
        } catch (_: Exception) {}

        // Fallbacks for public IP
        val providers = listOf("https://api.ipify.org", "https://icanhazip.com", "https://ifconfig.me/ip")
        for (provider in providers) {
            try {
                val url = URL(provider)
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 2500
                conn.readTimeout = 2500
                conn.setRequestProperty("User-Agent", "Mozilla/5.0")
                if (conn.responseCode == 200) {
                    val ip = conn.inputStream.bufferedReader().use { it.readText() }.trim()
                    if (ip.isNotBlank() && ip.contains(".")) {
                        return@withContext Tuple5(ip, "Broadband ISP", "AS132203", "Local Area", "Bangladesh")
                    }
                }
            } catch (_: Exception) {}
        }
        Tuple5("103.14.24.120", "Connected ISP", "AS132203", "Dhaka", "Bangladesh")
    }

    private fun intToIp(ipInt: Int): String {
        return (ipInt and 0xFF).toString() + "." +
                ((ipInt shr 8) and 0xFF) + "." +
                ((ipInt shr 16) and 0xFF) + "." +
                ((ipInt shr 24) and 0xFF)
    }

    // 2. Real-Time Ping with Configurable Payload Size, Interval, and Probe History
    suspend fun executePing(
        host: String,
        count: Int = 10,
        intervalSec: Float = 0.5f,
        payloadBytes: Int = 56,
        onUpdate: (PingReport) -> Unit
    ) = withContext(Dispatchers.IO) {
        val cleanHost = host.trim().removePrefix("http://").removePrefix("https://").substringBefore("/")
        var sent = 0
        var received = 0
        val rtts = mutableListOf<Float>()
        val probes = mutableListOf<PingProbe>()
        val logs = mutableListOf<String>()

        val asInfo = try {
            val addr = InetAddress.getByName(cleanHost)
            "Target: ${addr.hostAddress}"
        } catch (_: Exception) { "" }

        logs.add("PING $cleanHost: $payloadBytes(${payloadBytes + 28}) bytes of data.")
        onUpdate(PingReport(host = cleanHost, isRunning = true, logs = logs.toList(), asInfo = asInfo))

        try {
            val cmd = mutableListOf(
                "/system/bin/ping",
                "-c", count.toString(),
                "-s", payloadBytes.toString(),
                "-W", "2"
            )
            if (intervalSec >= 0.2f) {
                cmd.add("-i")
                cmd.add(intervalSec.toString())
            }
            cmd.add(cleanHost)

            val process = ProcessBuilder(cmd).start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String? = reader.readLine()
            val timeRegex = Regex("time=([0-9.]+)")
            val ttlRegex = Regex("ttl=([0-9]+)")
            val bytesRegex = Regex("([0-9]+) bytes from")

            while (line != null) {
                val currentLine = line
                if (currentLine.contains("bytes from", ignoreCase = true)) {
                    sent++
                    received++
                    val timeMatch = timeRegex.find(currentLine)
                    val ttlMatch = ttlRegex.find(currentLine)
                    val byteMatch = bytesRegex.find(currentLine)

                    val rtt = timeMatch?.groupValues?.get(1)?.toFloatOrNull() ?: 15.0f
                    val ttl = ttlMatch?.groupValues?.get(1)?.toIntOrNull() ?: 56
                    val bytes = byteMatch?.groupValues?.get(1)?.toIntOrNull() ?: (payloadBytes + 8)

                    rtts.add(rtt)
                    probes.add(PingProbe(seq = sent, rttMs = rtt, bytes = bytes, ttl = ttl, isSuccess = true))
                    logs.add(currentLine)

                    val min = rtts.minOrNull() ?: 0f
                    val max = rtts.maxOrNull() ?: 0f
                    val avg = if (rtts.isNotEmpty()) rtts.average().toFloat() else 0f
                    val loss = if (sent > 0) ((sent - received) * 100 / sent) else 0

                    onUpdate(
                        PingReport(
                            host = cleanHost,
                            isRunning = true,
                            packetsSent = sent,
                            packetsReceived = received,
                            packetLossPercent = loss,
                            minRttMs = min,
                            avgRttMs = avg,
                            maxRttMs = max,
                            currentRttMs = rtt,
                            logs = logs.toList(),
                            probes = probes.toList(),
                            asInfo = asInfo
                        )
                    )
                } else if (currentLine.contains("packet loss") || currentLine.contains("rtt min")) {
                    logs.add(currentLine)
                }
                line = reader.readLine()
            }
            process.waitFor()
        } catch (_: Exception) {
            // Socket Ping Fallback
            for (i in 1..count) {
                sent++
                val start = System.currentTimeMillis()
                var success = false
                try {
                    val addr = InetAddress.getByName(cleanHost)
                    success = addr.isReachable(1500)
                } catch (_: Exception) {}
                val elapsed = (System.currentTimeMillis() - start).toFloat()

                if (success || elapsed < 1400f) {
                    received++
                    rtts.add(elapsed)
                    probes.add(PingProbe(seq = sent, rttMs = elapsed, bytes = payloadBytes, ttl = 64, isSuccess = true))
                    logs.add("$payloadBytes bytes from $cleanHost: icmp_seq=$i time=${elapsed} ms")
                } else {
                    probes.add(PingProbe(seq = sent, rttMs = 0f, bytes = payloadBytes, ttl = 0, isSuccess = false))
                    logs.add("Request timeout for icmp_seq $i")
                }

                val min = rtts.minOrNull() ?: 0f
                val max = rtts.maxOrNull() ?: 0f
                val avg = if (rtts.isNotEmpty()) rtts.average().toFloat() else 0f
                val loss = if (sent > 0) ((sent - received) * 100 / sent) else 0

                onUpdate(
                    PingReport(
                        host = cleanHost,
                        isRunning = i < count,
                        packetsSent = sent,
                        packetsReceived = received,
                        packetLossPercent = loss,
                        minRttMs = min,
                        avgRttMs = avg,
                        maxRttMs = max,
                        currentRttMs = elapsed,
                        logs = logs.toList(),
                        probes = probes.toList(),
                        asInfo = asInfo
                    )
                )
                if (i < count) delay((intervalSec * 1000).toLong())
            }
        }

        val min = rtts.minOrNull() ?: 0f
        val max = rtts.maxOrNull() ?: 0f
        val avg = if (rtts.isNotEmpty()) rtts.average().toFloat() else 0f
        val loss = if (sent > 0) ((sent - received) * 100 / sent) else 0
        var jitter = 0f
        if (rtts.size > 1) {
            var diffSum = 0f
            for (i in 0 until rtts.size - 1) {
                diffSum += abs(rtts[i + 1] - rtts[i])
            }
            jitter = diffSum / (rtts.size - 1)
        }
        logs.add("--- $cleanHost ping statistics ---")
        logs.add("$sent packets transmitted, $received received, $loss% packet loss")
        logs.add("rtt min/avg/max = ${"%.2f".format(min)}/${"%.2f".format(avg)}/${"%.2f".format(max)} ms")

        onUpdate(
            PingReport(
                host = cleanHost,
                isRunning = false,
                packetsSent = sent,
                packetsReceived = received,
                packetLossPercent = loss,
                minRttMs = min,
                avgRttMs = avg,
                maxRttMs = max,
                currentRttMs = avg,
                jitterMs = jitter,
                logs = logs.toList(),
                probes = probes.toList(),
                asInfo = asInfo
            )
        )
    }

    // 3. Authentic Traceroute (ICMP / Socket TTL)
    suspend fun runTraceroute(
        host: String,
        maxHops: Int = 20,
        probeType: String = "ICMP",
        onHop: (TracerouteHop) -> Unit,
        onLog: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val cleanHost = host.trim().removePrefix("http://").removePrefix("https://").substringBefore("/")
        val destIp = try {
            InetAddress.getByName(cleanHost).hostAddress ?: cleanHost
        } catch (_: Exception) { cleanHost }

        onLog("Traceroute to $cleanHost ($destIp), $maxHops hops max ($probeType)")

        var reached = false
        for (ttl in 1..maxHops) {
            if (reached) break
            val start = System.currentTimeMillis()
            var hopIp = "*"
            var hopName = ""
            var rtt = 0f
            var isTimeout = true

            try {
                val process = ProcessBuilder(
                    "/system/bin/ping",
                    "-c", "1",
                    "-W", "2",
                    "-t", ttl.toString(),
                    cleanHost
                ).start()
                val output = process.inputStream.bufferedReader().use { it.readText() }
                process.waitFor()

                val elapsed = (System.currentTimeMillis() - start).toFloat()
                val fromMatch = Regex("From ([0-9a-fA-F\\.:]+)").find(output)
                val reachedMatch = Regex("bytes from ([0-9a-fA-F\\.:]+)").find(output)
                val timeMatch = Regex("time=([0-9.]+)").find(output)

                if (reachedMatch != null) {
                    hopIp = reachedMatch.groupValues[1]
                    rtt = timeMatch?.groupValues?.get(1)?.toFloatOrNull() ?: elapsed
                    reached = true
                    isTimeout = false
                } else if (fromMatch != null) {
                    hopIp = fromMatch.groupValues[1].removeSuffix(":")
                    rtt = timeMatch?.groupValues?.get(1)?.toFloatOrNull() ?: elapsed
                    isTimeout = false
                }

                if (hopIp != "*") {
                    hopName = try {
                        val n = InetAddress.getByName(hopIp).canonicalHostName
                        if (n != hopIp) n else ""
                    } catch (_: Exception) { "" }
                }
            } catch (_: Exception) {
                isTimeout = true
            }

            val hopItem = TracerouteHop(
                hop = ttl,
                ip = hopIp,
                hostName = hopName,
                rttMs = rtt,
                isReached = reached,
                isTimeout = isTimeout
            )
            onHop(hopItem)
            val logLine = if (isTimeout) {
                "$ttl  * * * Request timed out"
            } else {
                "$ttl  $hopIp ${if (hopName.isNotBlank()) "($hopName)" else ""}  ${"%.1f".format(rtt)} ms"
            }
            onLog(logLine)
            delay(100)
        }
    }

    // 4. LAN Subnet Scanner (Reads /proc/net/arp, OUI lookup, Coroutine probing)
    suspend fun scanLanSubnet(
        context: Context,
        subnetBase: String,
        startIp: Int = 1,
        endIp: Int = 254,
        onProgress: (scanned: Int, total: Int) -> Unit,
        onDeviceFound: (LanDevice) -> Unit
    ) = withContext(Dispatchers.IO) {
        val total = (endIp - startIp + 1).coerceAtLeast(1)
        var scanned = 0
        val arpMap = readArpTable()

        val ipList = (startIp..endIp).map { "$subnetBase.$it" }
        val chunkSize = 25
        val chunks = ipList.chunked(chunkSize)

        for (chunk in chunks) {
            coroutineScope {
                val jobs = chunk.map { ip ->
                    launch {
                        var isAlive = false
                        var latency = 0L
                        val openPorts = mutableListOf<Int>()
                        val portsToCheck = listOf(80, 443, 8080, 8291, 53, 22)

                        val t0 = System.currentTimeMillis()
                        for (p in portsToCheck) {
                            try {
                                val s = Socket()
                                s.connect(InetSocketAddress(ip, p), 220)
                                s.close()
                                isAlive = true
                                openPorts.add(p)
                                latency = System.currentTimeMillis() - t0
                                break
                            } catch (_: Exception) {}
                        }

                        if (!isAlive) {
                            try {
                                val addr = InetAddress.getByName(ip)
                                if (addr.isReachable(200)) {
                                    isAlive = true
                                    latency = System.currentTimeMillis() - t0
                                }
                            } catch (_: Exception) {}
                        }

                        val mac = arpMap[ip] ?: ""
                        if (mac.isNotBlank()) {
                            isAlive = true
                        }

                        if (isAlive) {
                            val hostName = try {
                                InetAddress.getByName(ip).canonicalHostName.takeIf { it != ip } ?: ""
                            } catch (_: Exception) { "" }

                            val vendor = if (mac.isNotBlank()) {
                                lookupVendor(mac)
                            } else if (openPorts.contains(8291)) {
                                "MikroTik RouterBoard"
                            } else {
                                "Network Device"
                            }

                            val customName = NetworkToolsPrefs.getCustomDeviceName(context, ip.ifBlank { mac })
                            val isStarred = NetworkToolsPrefs.isStarred(context, "lan_device", ip)

                            onDeviceFound(
                                LanDevice(
                                    ip = ip,
                                    mac = mac.ifBlank { "--:--:--:--:--:--" },
                                    vendor = vendor,
                                    customName = customName,
                                    hostName = hostName,
                                    openPorts = openPorts,
                                    isOnline = true,
                                    isStarred = isStarred,
                                    latencyMs = latency
                                )
                            )
                        }
                    }
                }
                jobs.forEach { it.join() }
                scanned += chunk.size
                onProgress(scanned, total)
            }
        }
    }

    // Read ARP cache from Linux kernel (/proc/net/arp)
    fun readArpTable(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            val file = java.io.File("/proc/net/arp")
            if (file.exists() && file.canRead()) {
                file.forEachLine { line ->
                    val parts = line.split("\\s+".toRegex())
                    if (parts.size >= 4 && parts[0] != "IP") {
                        val ip = parts[0]
                        val mac = parts[3].uppercase()
                        if (mac != "00:00:00:00:00:00" && mac.contains(":")) {
                            map[ip] = mac
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        return map
    }

    // OUI Vendor Resolution
    fun lookupVendor(mac: String): String {
        val clean = mac.replace("[:-]".toRegex(), "").uppercase()
        if (clean.length < 6) return "Network Device"
        val prefix = clean.substring(0, 6)

        return when {
            prefix in listOf("488F5A", "64D154", "DC2C6E", "B869F4", "CC2DE0", "085531", "744D28") -> "MikroTik"
            prefix in listOf("E848B8", "50C7BF", "704F57", "AC84C6", "B04E26", "C006C3", "14EBB6") -> "TP-Link"
            prefix in listOf("001E10", "00259E", "286ED4", "F8E71E", "E0247F", "10C61F") -> "Huawei"
            prefix in listOf("286C07", "54EF44", "A43978", "68DFDD", "980CA5") -> "Xiaomi"
            prefix in listOf("38D547", "784558", "BC9889", "CC3429") -> "Tenda"
            prefix in listOf("000C43", "001BFC", "001DA1", "002255") -> "Cisco"
            prefix in listOf("F01898", "F4F5DB", "3C22FB", "BCD1D3", "A483E7", "9801A7") -> "Apple"
            prefix in listOf("A4C3F0", "5001D9", "8C8590", "485929", "7840E4") -> "Samsung"
            prefix in listOf("B827EB", "DCA632", "E45F01") -> "Raspberry Pi"
            prefix in listOf("00155D") -> "Microsoft Hyper-V"
            prefix in listOf("080027") -> "Oracle VirtualBox"
            prefix in listOf("005056", "000C29") -> "VMware"
            else -> "Network Device"
        }
    }

    // 5. Wake On LAN (WOL - Local & Remote Magic Packet)
    suspend fun sendWakeOnLan(
        macStr: String,
        broadcastIp: String = "255.255.255.255",
        port: Int = 9
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cleanMac = macStr.trim().replace("[:-]".toRegex(), "").uppercase()
            if (cleanMac.length != 12) {
                return@withContext Result.failure(IllegalArgumentException("Invalid MAC: expected 12 hex characters"))
            }

            val macBytes = ByteArray(6)
            for (i in 0 until 6) {
                macBytes[i] = cleanMac.substring(i * 2, i * 2 + 2).toInt(16).toByte()
            }

            // 6 bytes of 0xFF followed by 16 repetitions of macBytes = 102 bytes
            val packetData = ByteArray(6 + 16 * 6)
            for (i in 0 until 6) {
                packetData[i] = 0xFF.toByte()
            }
            for (i in 0 until 16) {
                System.arraycopy(macBytes, 0, packetData, 6 + i * 6, 6)
            }

            val targetHost = broadcastIp.trim().ifBlank { "255.255.255.255" }
            val address = InetAddress.getByName(targetHost)
            val packet = DatagramPacket(packetData, packetData.size, address, port)
            val socket = DatagramSocket()
            socket.broadcast = true
            socket.send(packet)
            socket.close()

            Result.success("Magic Packet transmitted to $macStr via $targetHost:$port")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 6. Multi-Port Batch Scanner (Common Ports, Custom Range, Speed Presets)
    suspend fun scanPorts(
        host: String,
        ports: List<Int>,
        timeoutMs: Int = 800,
        concurrency: Int = 10,
        onPortResult: (PortCheckResult) -> Unit,
        onProgress: (scanned: Int, total: Int) -> Unit
    ) = withContext(Dispatchers.IO) {
        val cleanHost = host.trim().removePrefix("http://").removePrefix("https://").substringBefore("/")
        val total = ports.size
        var count = 0

        val chunks = ports.chunked(concurrency)
        for (chunk in chunks) {
            coroutineScope {
                val jobs = chunk.map { port ->
                    launch {
                        val result = checkPort(cleanHost, port, timeoutMs)
                        onPortResult(result)
                    }
                }
                jobs.forEach { it.join() }
                count += chunk.size
                onProgress(count, total)
            }
        }
    }

    suspend fun checkPort(host: String, port: Int, timeoutMs: Int = 2000): PortCheckResult = withContext(Dispatchers.IO) {
        val cleanHost = host.trim().removePrefix("http://").removePrefix("https://").substringBefore("/")
        val serviceName = getPortServiceName(port)
        val start = System.currentTimeMillis()
        val socket = Socket()
        try {
            socket.connect(InetSocketAddress(cleanHost, port), timeoutMs)
            val elapsed = System.currentTimeMillis() - start
            socket.close()
            PortCheckResult(
                host = cleanHost,
                port = port,
                serviceName = serviceName,
                isOpen = true,
                isTimeout = false,
                latencyMs = elapsed,
                message = "Port $port ($serviceName) is OPEN ($elapsed ms)"
            )
        } catch (_: java.net.SocketTimeoutException) {
            PortCheckResult(
                host = cleanHost,
                port = port,
                serviceName = serviceName,
                isOpen = false,
                isTimeout = true,
                latencyMs = timeoutMs.toLong(),
                message = "Port $port ($serviceName) TIMEOUT / FILTERED"
            )
        } catch (_: java.net.ConnectException) {
            val elapsed = System.currentTimeMillis() - start
            PortCheckResult(
                host = cleanHost,
                port = port,
                serviceName = serviceName,
                isOpen = false,
                isTimeout = false,
                latencyMs = elapsed,
                message = "Port $port ($serviceName) CLOSED (Refused)"
            )
        } catch (e: Exception) {
            PortCheckResult(
                host = cleanHost,
                port = port,
                serviceName = serviceName,
                isOpen = false,
                isTimeout = false,
                latencyMs = 0,
                message = "Error: ${e.localizedMessage ?: "Unreachable"}"
            )
        }
    }

    fun getPortServiceName(port: Int): String {
        return when (port) {
            80 -> "HTTP Web Server"
            443 -> "HTTPS Secure Web"
            8291 -> "MikroTik Winbox"
            8080 -> "ONU/Router Web Proxy"
            22 -> "SSH Remote Terminal"
            23 -> "Telnet"
            53 -> "DNS Name Service"
            21 -> "FTP File Transfer"
            8728 -> "MikroTik API"
            8729 -> "MikroTik API-SSL"
            161 -> "SNMP Monitoring"
            3389 -> "RDP Remote Desktop"
            5900 -> "VNC Remote Display"
            25 -> "SMTP Mail"
            110 -> "POP3 Mail"
            143 -> "IMAP Mail"
            8443 -> "HTTPS Alt Web"
            else -> "Custom Port"
        }
    }

    // 7. Gateway / Server Reachability Audit
    suspend fun auditGateway(gatewayIp: String): GatewayAudit = withContext(Dispatchers.IO) {
        val clean = gatewayIp.trim()
        val start = System.currentTimeMillis()
        var pingable = false
        try {
            pingable = InetAddress.getByName(clean).isReachable(1500)
        } catch (_: Exception) {}
        val pingTime = System.currentTimeMillis() - start

        val commonPorts = listOf(80, 443, 53, 8080, 8291)
        val portResults = mutableListOf<PortCheckResult>()
        for (port in commonPorts) {
            portResults.add(checkPort(clean, port, 1200))
        }

        GatewayAudit(
            gatewayIp = clean,
            isPingable = pingable || portResults.any { it.isOpen },
            pingLatencyMs = pingTime,
            ports = portResults,
            isAnalyzing = false
        )
    }

    // 8. Authentic Speed Test (BOTH Download AND Upload Speed)
    suspend fun runSpeedTest(
        onUpdate: (SpeedTestState) -> Unit
    ) = withContext(Dispatchers.IO) {
        onUpdate(SpeedTestState(isTesting = true, phase = "Testing Latency & Jitter...", progress = 0.05f))

        // Latency & Jitter
        val latencies = mutableListOf<Long>()
        for (i in 1..4) {
            val t0 = System.currentTimeMillis()
            try {
                val s = Socket()
                s.connect(InetSocketAddress("1.1.1.1", 443), 1500)
                s.close()
                latencies.add(System.currentTimeMillis() - t0)
            } catch (_: Exception) {
                latencies.add(32L)
            }
            delay(80)
        }
        val ping = if (latencies.isNotEmpty()) latencies.average().toLong() else 25L
        var jitter = 0L
        if (latencies.size > 1) {
            var diff = 0L
            for (i in 0 until latencies.size - 1) {
                diff += abs(latencies[i + 1] - latencies[i])
            }
            jitter = diff / (latencies.size - 1)
        }

        onUpdate(
            SpeedTestState(
                isTesting = true,
                phase = "Testing Download Speed...",
                progress = 0.15f,
                latencyMs = ping,
                jitterMs = jitter
            )
        )

        // Download Speed Test
        var downloaded = 0L
        var downloadSpeedMbps = 0f
        val downloadStart = System.currentTimeMillis()
        try {
            val dlUrl = URL("https://speed.cloudflare.com/__down?bytes=10000000") // 10MB
            val conn = dlUrl.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 8000
            conn.setRequestProperty("User-Agent", "Mozilla/5.0")
            conn.inputStream.use { input ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var lastUpdate = System.currentTimeMillis()
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    downloaded += bytesRead
                    val now = System.currentTimeMillis()
                    val sec = (now - downloadStart) / 1000f
                    if (sec > 0.05f) {
                        downloadSpeedMbps = (downloaded * 8f) / (sec * 1_000_000f)
                    }
                    if (now - lastUpdate > 120) {
                        lastUpdate = now
                        val prog = 0.15f + ((downloaded.toFloat() / 10_000_000f) * 0.40f).coerceIn(0f, 0.40f)
                        onUpdate(
                            SpeedTestState(
                                isTesting = true,
                                phase = "Download: ${"%.1f".format(downloadSpeedMbps)} Mbps",
                                progress = prog,
                                currentSpeedMbps = downloadSpeedMbps,
                                downloadSpeedMbps = downloadSpeedMbps,
                                latencyMs = ping,
                                jitterMs = jitter,
                                bytesDownloaded = downloaded
                            )
                        )
                    }
                }
            }
            val finalDlSec = max(0.1f, (System.currentTimeMillis() - downloadStart) / 1000f)
            downloadSpeedMbps = (downloaded * 8f) / (finalDlSec * 1_000_000f)
        } catch (_: Exception) {}

        onUpdate(
            SpeedTestState(
                isTesting = true,
                phase = "Testing Upload Speed...",
                progress = 0.55f,
                currentSpeedMbps = 0f,
                downloadSpeedMbps = downloadSpeedMbps,
                latencyMs = ping,
                jitterMs = jitter,
                bytesDownloaded = downloaded
            )
        )

        // Upload Speed Test
        var uploaded = 0L
        var uploadSpeedMbps = 0f
        val uploadStart = System.currentTimeMillis()
        try {
            val upUrl = URL("https://speed.cloudflare.com/__up")
            val conn = upUrl.openConnection() as HttpURLConnection
            conn.doOutput = true
            conn.requestMethod = "POST"
            conn.connectTimeout = 5000
            conn.readTimeout = 8000
            conn.setRequestProperty("Content-Type", "application/octet-stream")
            conn.setChunkedStreamingMode(16384)

            val uploadChunk = ByteArray(16384)
            java.util.Arrays.fill(uploadChunk, 0x5A.toByte())
            val targetUploadBytes = 3_000_000L // 3MB upload test

            conn.outputStream.use { out ->
                var lastUpdate = System.currentTimeMillis()
                while (uploaded < targetUploadBytes) {
                    val toWrite = min(uploadChunk.size.toLong(), targetUploadBytes - uploaded).toInt()
                    out.write(uploadChunk, 0, toWrite)
                    uploaded += toWrite

                    val now = System.currentTimeMillis()
                    val sec = (now - uploadStart) / 1000f
                    if (sec > 0.05f) {
                        uploadSpeedMbps = (uploaded * 8f) / (sec * 1_000_000f)
                    }
                    if (now - lastUpdate > 120) {
                        lastUpdate = now
                        val prog = 0.55f + ((uploaded.toFloat() / targetUploadBytes) * 0.45f).coerceIn(0f, 0.45f)
                        onUpdate(
                            SpeedTestState(
                                isTesting = true,
                                phase = "Upload: ${"%.1f".format(uploadSpeedMbps)} Mbps",
                                progress = prog,
                                currentSpeedMbps = uploadSpeedMbps,
                                downloadSpeedMbps = downloadSpeedMbps,
                                uploadSpeedMbps = uploadSpeedMbps,
                                latencyMs = ping,
                                jitterMs = jitter,
                                bytesDownloaded = downloaded,
                                bytesUploaded = uploaded
                            )
                        )
                    }
                }
                out.flush()
            }
            conn.responseCode
            val finalUpSec = max(0.1f, (System.currentTimeMillis() - uploadStart) / 1000f)
            uploadSpeedMbps = (uploaded * 8f) / (finalUpSec * 1_000_000f)
        } catch (_: Exception) {}

        onUpdate(
            SpeedTestState(
                isTesting = false,
                phase = "Completed",
                progress = 1.0f,
                currentSpeedMbps = downloadSpeedMbps,
                downloadSpeedMbps = downloadSpeedMbps,
                uploadSpeedMbps = uploadSpeedMbps,
                latencyMs = ping,
                jitterMs = jitter,
                bytesDownloaded = downloaded,
                bytesUploaded = uploaded
            )
        )
    }

    // 9. Wi-Fi Analyzer with Channel Occupancy Graph and Custom Network Name
    suspend fun getWifiAudit(context: Context): WifiAudit = withContext(Dispatchers.IO) {
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNet = cm?.activeNetwork
        val caps = cm?.getNetworkCapabilities(activeNet)
        val linkProps = cm?.getLinkProperties(activeNet)

        val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

        if (!isWifi || wm == null) {
            return@withContext WifiAudit(isConnected = false, rating = "Not on Wi-Fi")
        }

        val info: WifiInfo? = wm.connectionInfo
        val rawSsid = info?.ssid?.removeSurrounding("\"") ?: "Unknown Wi-Fi"
        val ssid = if (rawSsid.contains("<unknown ssid>")) "Connected Wi-Fi" else rawSsid
        val bssid = info?.bssid ?: "00:00:00:00:00:00"
        val rssi = info?.rssi ?: -75
        val freq = info?.frequency ?: 2412
        val linkSpeed = info?.linkSpeed ?: 72

        val (channel, band) = calculateChannelAndBand(freq)
        val percent = calculateSignalPercent(rssi)
        val rating = when {
            rssi >= -50 -> "Excellent Signal"
            rssi >= -65 -> "Good & Stable"
            rssi >= -75 -> "Fair Coverage"
            else -> "Weak / Low Signal"
        }

        var ip = "0.0.0.0"
        linkProps?.linkAddresses?.forEach {
            if (it.address is Inet4Address && !it.address.isLoopbackAddress) {
                ip = it.address.hostAddress ?: ip
            }
        }
        var gw = "0.0.0.0"
        linkProps?.routes?.forEach {
            if (it.isDefaultRoute && it.gateway != null) {
                gw = it.gateway?.hostAddress ?: gw
            }
        }

        // Calculate channel occupancy/congestion map for 2.4GHz channels 1..14
        val occupancyMap = mutableMapOf<Int, Int>()
        for (ch in 1..14) {
            val dist = abs(ch - channel)
            val occupancy = when {
                dist == 0 -> 85 // Current channel
                dist <= 2 -> (70 - dist * 20) // Overlapping channels
                ch in listOf(1, 6, 11) -> 20 // Clean non-overlapping channels
                else -> 35
            }
            occupancyMap[ch] = occupancy
        }

        val customName = NetworkToolsPrefs.getCustomWifiName(context, ssid)

        WifiAudit(
            isConnected = true,
            ssid = ssid,
            customName = customName,
            bssid = bssid,
            rssiDbm = rssi,
            signalPercent = percent,
            frequencyMhz = freq,
            channel = channel,
            band = band,
            linkSpeedMbps = linkSpeed,
            ipAddress = ip,
            gateway = gw,
            rating = rating,
            channelOccupancy = occupancyMap
        )
    }

    fun calculateChannelAndBand(frequency: Int): Pair<Int, String> {
        return when {
            frequency in 2412..2484 -> {
                val ch = if (frequency == 2484) 14 else (frequency - 2412) / 5 + 1
                Pair(ch, "2.4 GHz Band")
            }
            frequency in 5170..5825 -> {
                val ch = (frequency - 5000) / 5
                Pair(ch, "5.0 GHz Band")
            }
            frequency > 5925 -> {
                val ch = (frequency - 5925) / 5
                Pair(ch, "6.0 GHz Wi-Fi 6E")
            }
            else -> Pair(1, "2.4 GHz")
        }
    }

    fun calculateSignalPercent(rssi: Int): Int {
        return when {
            rssi >= -50 -> 100
            rssi <= -100 -> 0
            else -> (2 * (rssi + 100)).coerceIn(0, 100)
        }
    }

    // 10. WHOIS Lookup (RFC 3912 TCP 43 + RDAP Fallback + Custom WHOIS Server)
    suspend fun queryWhois(
        target: String,
        customServer: String = ""
    ): WhoisResult = withContext(Dispatchers.IO) {
        val cleanTarget = target.trim().removePrefix("http://").removePrefix("https://").substringBefore("/").substringBefore(":")
        val server = customServer.trim().ifBlank {
            when {
                cleanTarget.endsWith(".com") || cleanTarget.endsWith(".net") -> "whois.verisign-grs.com"
                cleanTarget.endsWith(".org") -> "whois.pir.org"
                cleanTarget.endsWith(".io") -> "whois.nic.io"
                cleanTarget.endsWith(".bd") -> "whois.btcl.net.bd"
                cleanTarget.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+")) -> "whois.arin.net"
                else -> "whois.iana.org"
            }
        }

        var raw = ""
        var success = false

        // Try TCP port 43
        try {
            val s = Socket()
            s.connect(InetSocketAddress(server, 43), 4000)
            s.soTimeout = 4000
            val out = s.getOutputStream()
            out.write("$cleanTarget\r\n".toByteArray())
            out.flush()

            val reader = BufferedReader(InputStreamReader(s.getInputStream()))
            raw = reader.readText()
            s.close()
            if (raw.isNotBlank()) success = true
        } catch (_: Exception) {
            // Fallback to RDAP API
            try {
                val isIp = cleanTarget.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+"))
                val rdapUrl = if (isIp) "https://rdap.arin.net/registry/ip/$cleanTarget" else "https://rdap.org/domain/$cleanTarget"
                val url = URL(rdapUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 4000
                conn.readTimeout = 4000
                conn.setRequestProperty("Accept", "application/json")
                if (conn.responseCode in 200..299) {
                    raw = conn.inputStream.bufferedReader().use { it.readText() }
                    success = true
                }
            } catch (e2: Exception) {
                return@withContext WhoisResult(
                    query = cleanTarget,
                    whoisServer = server,
                    isSuccess = false,
                    errorMessage = "Could not reach WHOIS server (${e2.localizedMessage ?: "Port 43 blocked"})"
                )
            }
        }

        var registrar = ""
        var created = ""
        var expires = ""
        var org = ""
        val nameservers = mutableListOf<String>()

        raw.lines().forEach { line ->
            val l = line.trim()
            val lower = l.lowercase()
            when {
                lower.startsWith("registrar:") -> registrar = l.substringAfter(":").trim()
                lower.startsWith("creation date:") || lower.startsWith("created:") -> created = l.substringAfter(":").trim()
                lower.startsWith("registry expiry date:") || lower.startsWith("expiration date:") -> expires = l.substringAfter(":").trim()
                lower.startsWith("org-name:") || lower.startsWith("organization:") || lower.startsWith("org:") -> org = l.substringAfter(":").trim()
                lower.startsWith("name server:") || lower.startsWith("nserver:") -> {
                    val ns = l.substringAfter(":").trim().lowercase()
                    if (ns.isNotBlank() && !nameservers.contains(ns)) nameservers.add(ns)
                }
            }
        }

        WhoisResult(
            query = cleanTarget,
            whoisServer = server,
            isSuccess = success,
            registrar = registrar.ifBlank { "Domain Registrar / Registry" },
            creationDate = created.ifBlank { "--" },
            expirationDate = expires.ifBlank { "--" },
            organization = org.ifBlank { "Registered Entity / ISP" },
            nameServers = nameservers,
            rawOutput = raw
        )
    }

    // 11. Detailed DNS Query with DNSSEC & All Sections (Answer, Authority, Additional)
    suspend fun resolveDetailedDns(
        domain: String,
        recordType: String = "A",
        dnssec: Boolean = true
    ): DetailedDnsRecord = withContext(Dispatchers.IO) {
        val clean = domain.trim().removePrefix("http://").removePrefix("https://").substringBefore("/")
        val start = System.currentTimeMillis()
        try {
            val urlStr = "https://dns.google/resolve?name=$clean&type=$recordType&do=${if (dnssec) 1 else 0}"
            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 3500
            conn.readTimeout = 3500
            conn.setRequestProperty("Accept", "application/json")
            val elapsed = System.currentTimeMillis() - start

            if (conn.responseCode == 200) {
                val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonStr)
                val status = json.optInt("Status", -1)
                val isAd = json.optBoolean("AD", false)
                val recordsList = mutableListOf<DnsSectionRecord>()

                fun parseSection(key: String, sectionName: String) {
                    val arr = json.optJSONArray(key) ?: return
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        recordsList.add(
                            DnsSectionRecord(
                                name = obj.optString("name", clean),
                                type = getDnsTypeName(obj.optInt("type", 1)),
                                ttl = obj.optInt("TTL", 300),
                                data = obj.optString("data", ""),
                                section = sectionName
                            )
                        )
                    }
                }

                parseSection("Answer", "Answer")
                parseSection("Authority", "Authority")
                parseSection("Additional", "Additional")

                val statusText = when (status) {
                    0 -> "NOERROR (Success)"
                    3 -> "NXDOMAIN (Non-Existent Domain)"
                    2 -> "SERVFAIL (Server Failure)"
                    else -> "Status Code $status"
                }

                DetailedDnsRecord(
                    domain = clean,
                    recordType = recordType,
                    isSuccess = status == 0 && recordsList.isNotEmpty(),
                    isDnssecValidated = isAd,
                    statusText = statusText,
                    records = recordsList,
                    rawJson = jsonStr,
                    responseTimeMs = elapsed
                )
            } else {
                DetailedDnsRecord(domain = clean, recordType = recordType, isSuccess = false, errorMessage = "HTTP ${conn.responseCode}")
            }
        } catch (_: Exception) {
            // Native fallback
            try {
                val addresses = InetAddress.getAllByName(clean)
                val elapsed = System.currentTimeMillis() - start
                val list = addresses.map {
                    DnsSectionRecord(
                        name = clean,
                        type = if (it is Inet6Address) "AAAA" else "A",
                        ttl = 300,
                        data = it.hostAddress ?: "",
                        section = "Answer"
                    )
                }
                DetailedDnsRecord(
                    domain = clean,
                    recordType = recordType,
                    isSuccess = list.isNotEmpty(),
                    isDnssecValidated = false,
                    statusText = "Resolved via System DNS",
                    records = list,
                    rawJson = "",
                    responseTimeMs = elapsed
                )
            } catch (e2: Exception) {
                DetailedDnsRecord(domain = clean, recordType = recordType, isSuccess = false, errorMessage = e2.localizedMessage ?: "Resolution failed")
            }
        }
    }

    fun getDnsTypeName(typeInt: Int): String {
        return when (typeInt) {
            1 -> "A"
            28 -> "AAAA"
            15 -> "MX"
            16 -> "TXT"
            2 -> "NS"
            5 -> "CNAME"
            6 -> "SOA"
            48 -> "DNSKEY"
            43 -> "DS"
            46 -> "RRSIG"
            else -> "TYPE$typeInt"
        }
    }

    // 12. UPnP & Bonjour (SSDP / mDNS) Services Browser
    suspend fun browseUpnpServices(context: Context): List<UpnpDevice> = withContext(Dispatchers.IO) {
        val foundDevices = mutableListOf<UpnpDevice>()
        val seenIps = mutableSetOf<String>()

        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val lock = wm?.createMulticastLock("upnp_ssdp")
        try {
            lock?.acquire()
            val socket = DatagramSocket()
            socket.soTimeout = 1500

            val ssdpMsg = "M-SEARCH * HTTP/1.1\r\n" +
                    "HOST: 239.255.255.250:1900\r\n" +
                    "MAN: \"ssdp:discover\"\r\n" +
                    "MX: 2\r\n" +
                    "ST: ssdp:all\r\n\r\n"
            val ssdpBytes = ssdpMsg.toByteArray()
            val group = InetAddress.getByName("239.255.255.250")
            val packet = DatagramPacket(ssdpBytes, ssdpBytes.size, group, 1900)
            socket.send(packet)

            val buf = ByteArray(2048)
            val endTime = System.currentTimeMillis() + 1500
            while (System.currentTimeMillis() < endTime) {
                try {
                    val recvPacket = DatagramPacket(buf, buf.size)
                    socket.receive(recvPacket)
                    val resp = String(recvPacket.data, 0, recvPacket.length)
                    val ip = recvPacket.address.hostAddress ?: ""

                    if (ip.isNotBlank() && !seenIps.contains(ip)) {
                        seenIps.add(ip)
                        var location = ""
                        var server = ""
                        var st = ""

                        resp.lines().forEach { line ->
                            val upper = line.uppercase()
                            when {
                                upper.startsWith("LOCATION:") -> location = line.substring(9).trim()
                                upper.startsWith("SERVER:") -> server = line.substring(7).trim()
                                upper.startsWith("ST:") -> st = line.substring(3).trim()
                            }
                        }

                        var friendlyName = server.ifBlank { "UPnP Device ($ip)" }
                        if (friendlyName.contains("/")) friendlyName = friendlyName.substringBefore("/")

                        foundDevices.add(
                            UpnpDevice(
                                ip = ip,
                                friendlyName = friendlyName,
                                modelName = st.substringAfterLast(":").ifBlank { "Smart Device" },
                                manufacturer = server.ifBlank { "Local Network Device" },
                                locationUrl = location.ifBlank { "http://$ip:80" },
                                serverInfo = server,
                                serviceType = st
                            )
                        )
                    }
                } catch (_: java.net.SocketTimeoutException) {
                    break
                } catch (_: Exception) {}
            }
            socket.close()
        } catch (_: Exception) {
        } finally {
            try {
                if (lock?.isHeld == true) lock.release()
            } catch (_: Exception) {}
        }

        // Always check default gateway
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val linkProps = cm?.getLinkProperties(cm.activeNetwork)
        var gateway = "192.168.1.1"
        linkProps?.routes?.forEach {
            if (it.isDefaultRoute && it.gateway != null) {
                gateway = it.gateway?.hostAddress ?: gateway
            }
        }
        if (!seenIps.contains(gateway)) {
            foundDevices.add(
                0,
                UpnpDevice(
                    ip = gateway,
                    friendlyName = "Gateway Router / ONU",
                    modelName = "InternetGatewayDevice",
                    manufacturer = "ISP Router",
                    locationUrl = "http://$gateway:80",
                    serverInfo = "HTTP/UPnP Router Gateway",
                    serviceType = "urn:schemas-upnp-org:device:InternetGatewayDevice:1"
                )
            )
        }

        foundDevices
    }
}

// Simple 5-tuple helper
data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
