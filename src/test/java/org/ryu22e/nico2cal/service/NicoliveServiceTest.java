package org.ryu22e.nico2cal.service;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import org.ryu22e.nico2cal.meta.NicoliveIndexMeta;
import org.ryu22e.nico2cal.meta.NicoliveMeta;
import org.ryu22e.nico2cal.model.Nicolive;
import org.ryu22e.nico2cal.model.NicoliveIndex;
import org.ryu22e.nico2cal.rome.module.NicoliveModule;
import org.slim3.datastore.Datastore;
import org.slim3.tester.AppEngineTestCase;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.Text;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

/**
 * @author ryu22e
 *
 */
public final class NicoliveServiceTest extends AppEngineTestCase {
    /**
     * 
     */
    private List<Key> testDataKeys = new LinkedList<Key>();

    /*
     * (non-Javadoc) {@inheritDoc}
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        NamespaceManager.set("test");

        // テストデータを登録する。
        testDataKeys.clear();
        Nicolive nicolinve = new Nicolive();
        nicolinve.setTitle("テスト");
        nicolinve.setDescription(new Text("テスト説明文"));
        DateTime datetime = new DateTime();
        datetime = datetime.minusDays(0);
        nicolinve.setOpenTime(datetime.toDate());
        nicolinve.setLink(new Link("http://ryu22e.org/0"));
        Key key = Datastore.put(nicolinve);
        testDataKeys.add(key);
    }

    /*
     * (non-Javadoc) {@inheritDoc}
     */
    @Override
    public void tearDown() throws Exception {
        // テストデータを削除する。
        if (0 < testDataKeys.size()) {
            Datastore.delete(testDataKeys);
        }
        NicoliveMeta n = NicoliveMeta.get();
        List<Key> keys =
                Datastore
                    .query(n)
                    .filter(n.title.startsWith("テスト"))
                    .asKeyList();
        if (0 < keys.size()) {
            Datastore.delete(keys);
        }

        super.tearDown();
    }

    /**
     * 
     */
    private NicoliveService service = new NicoliveService();

    /**
     * テスト用のRSSフィードを生成する。
     * @return テスト用のRSSフィード
     */
    @SuppressWarnings("unchecked")
    private SyndFeed createFeed() {
        SyndFeed feed = new SyndFeedImpl();
        feed.setModules(Arrays.asList(new NicoliveModule()));

        feed.setTitle("テストRSS");

        DateTime datetime = new DateTime(2011, 1, 1, 0, 0, 0, 0);
        DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        List<SyndEntry> entries = new LinkedList<SyndEntry>();
        for (int i = 0; i < 9; i++) {
            SyndEntry entry = new SyndEntryImpl();
            entry.setTitle("テスト" + i);
            SyndContent description = new SyndContentImpl();
            description.setValue("テスト説明" + i);
            entry.setDescription(description);
            entry.setLink("http://ryu22e.org/" + i);

            NicoliveModule module = new NicoliveModule();
            module.setOpenTime(datetime.minusDays(i).toString(df));
            module.setStartTime(datetime
                .minusDays(i)
                .plusMinutes(10)
                .toString(df));
            module.setType("official");
            entry.getModules().add(module);

            entries.add(entry);
        }
        // NicoliveModuleなしのデータも含める。
        SyndEntry invalidEntry = new SyndEntryImpl();
        invalidEntry.setTitle("NicoliveModuleなしデータ");
        SyndContent description = new SyndContentImpl();
        description.setValue("テスト説明");
        invalidEntry.setDescription(description);
        invalidEntry.setLink("http://ryu22e.org/");
        entries.add(invalidEntry);

        feed.setEntries(entries);

        return feed;
    }

    /**
     * @throws Exception
     */
    @Test(expected = NullPointerException.class)
    public void RSSフィードをデータストアに登録する_パラメータがnull() throws Exception {
        assertThat(service, is(notNullValue()));

        service.put(null);
    }

    /**
     * @throws Exception
     */
    @Test(expected = NullPointerException.class)
    public void データストアに登録したRSSデータを取得する_パラメータがnull() throws Exception {
        assertThat(service, is(notNullValue()));

        service.find(null);
    }

    @Test
    public void データストアに登録したRSSデータを取得する_該当するデータが存在しない() throws Exception {
        assertThat(service, is(notNullValue()));

        Key key = Datastore.allocateId(NicoliveMeta.get());
        Nicolive nicolive = service.find(key);
        assertThat(nicolive, is(nullValue()));
    }

    /**
     * @throws Exception
     */
    @Test
    public void データストアに登録したRSSデータを取得する_該当するデータが存在する() throws Exception {
        assertThat(service, is(notNullValue()));

        // テストデータを登録する。
        Nicolive testData = new Nicolive();
        testData.setTitle("テストデータ");
        testData.setDescription(new Text("これはテストデータです。"));
        DateTime openTime = new DateTime();
        testData.setOpenTime(openTime.toDate());
        DateTime startTime = openTime.minusMinutes(10);
        testData.setStartTime(startTime.toDate());
        testData.setLink(new Link("http://ryu22e.org/"));
        testData.setType("official");

        Key key = Datastore.put(testData);
        testDataKeys.add(key);

        Nicolive nicolive = service.find(key);
        assertThat(nicolive, is(notNullValue()));
        assertThat(nicolive, is(testData));
    }

    /**
     * @throws Exception
     */
    @Test(expected = NullPointerException.class)
    public void データストアに登録したRSSデータListを取得する_パラメータがnull() throws Exception {
        assertThat(service, is(notNullValue()));

        service.findList(null);
    }

    /**
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void データストアに登録したRSSデータListを取得する_パラメータが不正() throws Exception {
        assertThat(service, is(notNullValue()));

        NicoliveCondition condition = new NicoliveCondition();
        condition.setStartDate(null);
        service.findList(condition);
    }

    /**
     * @throws Exception
     */
    @Test
    public void データストアに登録したRSSデータListを取得する_StartDateとEndDateを指定する_該当するデータが存在する()
            throws Exception {
        assertThat(service, is(notNullValue()));

        Datastore.delete(testDataKeys);
        testDataKeys.clear();

        // テストデータを登録する。
        for (int i = 0; i < 99; i++) {
            Nicolive nicolinve = new Nicolive();
            nicolinve.setTitle("テスト" + i);
            nicolinve.setDescription(new Text("テスト説明文" + i));
            DateTime datetime = new DateTime(2011, 1, 1, 0, 0, 0, 0);
            datetime = datetime.minusDays(i);
            nicolinve.setOpenTime(datetime.toDate());
            nicolinve.setLink(new Link("http://ryu22e.org/" + i));
            Key key = Datastore.put(nicolinve);
            testDataKeys.add(key);
        }

        NicoliveCondition condition = new NicoliveCondition();
        DateTime endDate = new DateTime(2010, 12, 31, 0, 0, 0, 0);
        DateTime startDate = endDate.minusDays(3);
        condition.setStartDate(startDate.toDate());
        condition.setEndDate(endDate.toDate());
        List<Nicolive> nicolives = service.findList(condition);
        assertThat(nicolives, is(notNullValue()));
        assertThat(nicolives.size(), is(4));

    }

    /**
     * @throws Exception
     */
    @Test
    public void データストアに登録したRSSデータListを取得する_該当するデータが存在する() throws Exception {
        assertThat(service, is(notNullValue()));

        Datastore.delete(testDataKeys);
        testDataKeys.clear();

        // テストデータを登録する。
        for (int i = 0; i < 99; i++) {
            Nicolive nicolinve = new Nicolive();
            nicolinve.setTitle("テスト" + i);
            nicolinve.setDescription(new Text("テスト説明文" + i));
            DateTime datetime = new DateTime(2011, 1, 1, 0, 0, 0, 0);
            datetime = datetime.minusDays(i);
            nicolinve.setOpenTime(datetime.toDate());
            nicolinve.setLink(new Link("http://ryu22e.org/" + i));
            Key key = Datastore.put(nicolinve);
            testDataKeys.add(key);
        }

        NicoliveCondition condition = new NicoliveCondition();
        DateTime datetime = new DateTime(2010, 12, 1, 0, 0, 0, 0);
        condition.setStartDate(datetime.toDate());
        List<Nicolive> nicolives = service.findList(condition);
        assertThat(nicolives, is(notNullValue()));
        assertThat(nicolives.size(), is(32));
    }

    /**
     * @throws Exception
     */
    @Test
    public void データストアに登録したRSSデータListを取得する_StartDateを指定する_該当するデータが存在しない()
            throws Exception {
        assertThat(service, is(notNullValue()));

        NicoliveCondition condition = new NicoliveCondition();
        DateTime datetime = new DateTime(2011, 12, 1, 0, 0, 0, 0);
        condition.setStartDate(datetime.toDate());
        List<Nicolive> nicolives = service.findList(condition);
        assertThat(nicolives, is(notNullValue()));
        assertThat(nicolives.size(), is(0));
    }

    /**
     * @throws Exception
     */
    @Test
    public void RSSフィードをデータストアに登録する() throws Exception {
        assertThat(service, is(notNullValue()));

        SyndFeed feed = createFeed();
        List<Key> keys = service.put(feed);
        assertThat(keys, is(notNullValue()));
        assertThat(keys.size(), is(8));

        NicoliveMeta n = NicoliveMeta.get();
        assertThat(
            Datastore
                .query(n)
                .filter(n.link.equal(new Link("http://ryu22e.org/0")))
                .count(),
            is(1));
        for (int i = 1; i < 9; i++) {
            int count =
                    Datastore
                        .query(n)
                        .filter(
                            n.link.equal(new Link("http://ryu22e.org/" + i)))
                        .count();
            assertThat(count, is(1));

            Nicolive nicolive =
                    Datastore
                        .query(n)
                        .filter(n.title.equal("テスト" + i))
                        .asSingle();
            assertThat(nicolive, is(notNullValue()));
            assertThat(nicolive.getTitle(), is("テスト" + i));
            assertThat(nicolive.getDescription(), is(notNullValue()));
            assertThat(nicolive.getDescription().getValue(), is("テスト説明" + i));
            assertThat(nicolive.getOpenTime(), is(notNullValue()));
            assertThat(nicolive.getStartTime(), is(notNullValue()));
            assertThat(nicolive.getType(), is("official"));
            assertThat(nicolive.getLink(), is(notNullValue()));
            assertThat(nicolive.getLink().getValue(), is("http://ryu22e.org/"
                    + i));
        }

        // NicoliveModuleがないエントリーは登録されない。
        int count =
                Datastore
                    .query(n)
                    .filter(n.title.equal("NicoliveModuleなしデータ"))
                    .count();
        assertThat(count, is(0));
    }

    /**
     * @throws Exception
     */
    @Test(expected = NullPointerException.class)
    public void 全文検索用インデックスを作成する_パラメータがnull() throws Exception {
        assertThat(service, is(notNullValue()));

        service.createIndex(null);
    }

    @Test
    public void 全文検索用インデックスを作成する() throws Exception {
        assertThat(service, is(notNullValue()));

        // テストデータを作成する。
        DateTime datetime = new DateTime();
        datetime = datetime.minusDays(3);
        Nicolive nicolinve1 = new Nicolive();
        nicolinve1.setTitle("テスト");
        nicolinve1.setDescription(new Text("本日は晴天なり。"));
        nicolinve1.setOpenTime(datetime.toDate());
        nicolinve1.setStartTime(datetime.plusMinutes(10).toDate());
        nicolinve1.setLink(new Link("http://ryu22e.org/1"));
        Key key1 = Datastore.put(nicolinve1);
        testDataKeys.add(key1);
        Nicolive nicolinve2 = new Nicolive();
        nicolinve2.setTitle("テスト");
        nicolinve2.setDescription(new Text("本日は晴天なり。"));
        nicolinve2.setOpenTime(datetime.toDate());
        nicolinve2.setStartTime(datetime.plusMinutes(10).toDate());
        nicolinve2.setLink(new Link("http://ryu22e.org/2"));
        Key key2 = Datastore.put(nicolinve2);
        testDataKeys.add(key2);
        NicoliveIndex nicoliveIndex = new NicoliveIndex();
        nicoliveIndex.setKeyword("テスト");
        nicoliveIndex.setNicoliveKey(key2);
        testDataKeys.add(Datastore.put(nicoliveIndex));

        service.createIndex(nicolinve1);

        // TitleとDescriptionが文節ごとに分解されて、各文節とNicoliveのKeyがエンティティに登録される。
        NicoliveIndexMeta n = NicoliveIndexMeta.get();
        testDataKeys.addAll(Datastore
            .query(n)
            .filter(n.nicoliveKey.equal(key1))
            .asKeyList());
        assertThat(
            Datastore
                .query(n)
                .filter(n.keyword.equal("テスト"), n.nicoliveKey.equal(key1))
                .count(),
            is(1));
        assertThat(
            Datastore
                .query(n)
                .filter(n.keyword.equal("本日"), n.nicoliveKey.equal(key1))
                .count(),
            is(1));
        assertThat(
            Datastore
                .query(n)
                .filter(n.keyword.equal("は"), n.nicoliveKey.equal(key1))
                .count(),
            is(1));
        assertThat(
            Datastore
                .query(n)
                .filter(n.keyword.equal("晴天"), n.nicoliveKey.equal(key1))
                .count(),
            is(1));
        assertThat(
            Datastore
                .query(n)
                .filter(n.keyword.equal("なり"), n.nicoliveKey.equal(key1))
                .count(),
            is(1));
        assertThat(
            Datastore
                .query(n)
                .filter(n.keyword.equal("。"), n.nicoliveKey.equal(key1))
                .count(),
            is(1));
    }

    /**
     * @throws Exception
     */
    @Test(expected = NullPointerException.class)
    public void 古い全文検索用インデックスを消す_パラメータがnull() throws Exception {
        assertThat(service, is(notNullValue()));

        service.deleteOldIndex(null);
    }

    /**
     * @throws Exception
     */
    @Test
    public void 古い全文検索用インデックスを消す() throws Exception {
        assertThat(service, is(notNullValue()));

        DateTime datetime = new DateTime();
        List<NicoliveIndex> indexes = new LinkedList<NicoliveIndex>();
        for (int i = 0; i < 50; i++) {
            NicoliveIndex index = new NicoliveIndex();
            index.setKeyword("テスト");
            index.setOpenTime(datetime.minusDays(i).toDate());
            indexes.add(index);
        }
        testDataKeys.addAll(Datastore.put(indexes));
        service.deleteOldIndex(datetime.minusDays(31).toDate());

        NicoliveIndexMeta ni = NicoliveIndexMeta.get();
        int count =
                Datastore
                    .query(ni)
                    .filter(
                        ni.openTime.lessThanOrEqual(datetime
                            .minusDays(31)
                            .toDate()))
                    .count();
        assertThat(count, is(0));
    }
}
