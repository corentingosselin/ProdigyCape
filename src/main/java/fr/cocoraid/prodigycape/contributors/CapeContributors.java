package fr.cocoraid.prodigycape.contributors;

import com.google.gson.Gson;
import fr.cocoraid.prodigycape.cape.Cape;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import java.util.Map;
import java.util.UUID;

public class CapeContributors {


    public record CapeResponse(String key, String name, String uuid, String texture, String description) {
    }

    private final Map<UUID, Cape> capeContributors = new HashMap<>();


    public CapeContributors() {
        loadContributors();
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        console.sendMessage("§a[ProdigyCape] Loaded " + capeContributors.size() + " contributors capes");

    }

    public void loadContributors() {
        // Define the Cloud Function URL
        String url = "https://europe-west2-prodigycape.cloudfunctions.net/contributors";


        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // Setting basic post request
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);


            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.write("{}".getBytes(StandardCharsets.UTF_8));
            }

            // Reading response from input Stream
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String output;
                StringBuffer response = new StringBuffer();

                while ((output = in.readLine()) != null) {
                    response.append(output);
                }

                Gson gson = new Gson();
                CapeResponse[] capeResponses = gson.fromJson(response.toString(), CapeResponse[].class);

                for (CapeResponse capeResponse : capeResponses) {
                    Cape cape = new Cape(
                            capeResponse.key,
                            false,
                            capeResponse.texture,
                            capeResponse.name,
                            capeResponse.description,
                            0,
                            0
                    );
                    capeContributors.put(UUID.fromString(capeResponse.uuid), cape);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<UUID, Cape> getCapeContributors() {
        return capeContributors;
    }

    public Cape getCape(UUID uuid) {
        return capeContributors.get(uuid);
    }
}
