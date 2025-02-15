package dev.apollo.artisly.tasks;

import com.algolia.search.SearchIndex;
import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.models.Product;
import dev.apollo.artisly.services.AlgoliaService;
import dev.apollo.artisly.services.ProductService;
import redis.clients.jedis.Jedis;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AlgoliaIndexerTask implements Runnable {

    public AlgoliaIndexerTask() {
    }

    @Override
    public void run() {
        AlgoliaService.processProductQueue();
    }

}
