package org.ryu22e.nico2cal.service;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.reduls.igo.Morpheme;
import net.reduls.igo.Tagger;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.ryu22e.nico2cal.meta.NicoliveIndexMeta;
import org.ryu22e.nico2cal.meta.NicoliveMeta;
import org.ryu22e.nico2cal.model.Nicolive;
import org.ryu22e.nico2cal.model.NicoliveIndex;
import org.ryu22e.nico2cal.rome.module.NicoliveModule;
import org.slim3.datastore.Datastore;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Text;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

/**
 * {@link Nicolive}を操作するサービスクラス。
 * @author ryu22e
 *
 */
/**
 * @author ryu22e
 *
 */
public final class NicoliveService {

    /**
     * RSSフィードをデータストアに登録する。
     * @param feed RSSフィード
     * @throws NullPointerException パラメータがnullの場合。
     * @return 登録したNicoliveのキー
     */
    public List<Key> put(SyndFeed feed) {
        if (feed == null) {
            throw new NullPointerException("feed is null.");
        }

        NicoliveMeta n = NicoliveMeta.get();
        DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        List<Nicolive> nicolives = new LinkedList<Nicolive>();
        @SuppressWarnings("unchecked")
        List<SyndEntry> entries = (List<SyndEntry>) feed.getEntries();
        for (SyndEntry entry : entries) {
            // RSSフィードの「nicolive:***」部分を取得する。
            NicoliveModule module = null;
            @SuppressWarnings("unchecked")
            List<Object> modules = entry.getModules();
            for (Object o : modules) {
                if (o instanceof NicoliveModule) {
                    module = (NicoliveModule) o;
                    break;
                }
            }

            // 「nicolive:***」が取得できないエントリーは登録しない。
            if (module != null) {
                // 重複するリンクを持つエンティティがある場合は、既存のエンティティを上書き更新する。
                Nicolive nicolive =
                        Datastore
                            .query(n)
                            .filter(n.link.equal(new Link(entry.getLink())))
                            .asSingle();
                if (nicolive == null) {
                    nicolive = new Nicolive();
                }

                nicolive.setTitle(entry.getTitle());
                nicolive.setDescription(new Text(entry
                    .getDescription()
                    .getValue()));
                nicolive.setOpenTime(df
                    .parseDateTime(module.getOpenTime())
                    .toDate());
                nicolive.setStartTime(df
                    .parseDateTime(module.getStartTime())
                    .toDate());
                nicolive.setType(module.getType());
                nicolive
                    .setPassword(Boolean.parseBoolean(module.getPassword()));
                nicolive.setPremiumOnly(Boolean.parseBoolean(module
                    .getPremiumOnly()));
                nicolive.setLink(new Link(entry.getLink()));

                nicolives.add(nicolive);
            }
        }

        return Datastore.put(nicolives);
    }

    /**
     * {@link Nicolive}の全文検索用インデックス{@link NicoliveIndex}を作成する。
     * @param nicolive インデックスを作成するNicoliveエンティティ。
     * @throws IOException Igoの辞書ファイル読み込みに失敗した場合。
     * @throws NullPointerException パラメータがnullの場合。
     * @return 登録したNicoliveIndexのキー
     */
    public List<Key> createIndex(Nicolive nicolive) throws IOException,
            InterruptedException {
        if (nicolive == null) {
            throw new NullPointerException("nicolive is null.");
        }

        // ここにインデックスを作る対象のキーワードを入れる。
        Set<String> keywords = new HashSet<String>();

        Tagger tagger = new Tagger("ipadic/");
        // Titleを文節ごとに分解する。
        List<Morpheme> titleMorphemes = tagger.parse(nicolive.getTitle());
        for (Morpheme morpheme : titleMorphemes) {
            keywords.add(morpheme.surface);
        }

        // Descriptionを文節ごとに分解する。
        List<Morpheme> descriptionMorphemes =
                tagger.parse(nicolive.getDescription().getValue());
        for (Morpheme morpheme : descriptionMorphemes) {
            keywords.add(morpheme.surface);
        }

        // 1キーワード1エンティティとして登録する。
        List<NicoliveIndex> indexes = new LinkedList<NicoliveIndex>();
        NicoliveIndexMeta n = NicoliveIndexMeta.get();
        for (String keyword : keywords) {
            int count =
                    Datastore
                        .query(n)
                        .filter(
                            n.keyword.equal(keyword),
                            n.nicoliveKey.equal(nicolive.getKey()))
                        .count();
            if (count <= 0) {
                NicoliveIndex index = new NicoliveIndex();
                index.setKey(Datastore.allocateId(n));
                index.setKeyword(keyword);
                index.setNicoliveKey(nicolive.getKey());
                DateTime now = new DateTime();
                index.setCreatedAt(now.toDate());
                indexes.add(index);
            }
        }
        return Datastore.put(indexes);
    }

    /**
     * 登録されている{@link Nicolive}のListを取得する。
     * @param condition 検索条件
     * @return {@link Nicolive}のリスト
     * @throws NullPointerException パラメータがnullの場合。
     * @throws IllegalArgumentException 検索条件にStartDateが指定されていない場合。
     */
    public List<Nicolive> findList(NicoliveCondition condition) {
        if (condition == null) {
            throw new NullPointerException("condition is null.");
        }
        if (condition.getStartDate() == null) {
            throw new IllegalArgumentException("StartDate is null.");
        }

        NicoliveMeta n = NicoliveMeta.get();
        if (condition.getEndDate() == null) {
            return Datastore
                .query(n)
                .filter(n.openTime.greaterThanOrEqual(condition.getStartDate()))
                .sort(n.openTime.getName(), SortDirection.ASCENDING)
                .asList();
        } else {
            return Datastore
                .query(n)
                .filter(
                    n.openTime.greaterThanOrEqual(condition.getStartDate()),
                    n.openTime.lessThanOrEqual(condition.getEndDate()))
                .sort(n.openTime.getName(), SortDirection.ASCENDING)
                .asList();
        }
    }

    /**
     * 登録されている{@link Nicolive}を取得する。
     * @param key {@link Nicolive}のキー
     * @return {@link Nicolive}
     * @throws NullPointerException パラメータがnullの場合。
     */
    public Nicolive find(Key key) {
        if (key == null) {
            throw new NullPointerException("key is null.");
        }
        NicoliveMeta n = NicoliveMeta.get();
        return Datastore.getOrNull(n, key);
    }

    /**
     * {@link NicoliveIndex}を全て削除する。
     */
    public void deleteAllIndex() {
        NicoliveIndexMeta ni = NicoliveIndexMeta.get();
        Datastore.delete(Datastore.query(ni).asKeyList());
    }
}
