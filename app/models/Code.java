package models;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import demo.Order;
import org.rythmengine.Rythm;
import org.rythmengine.RythmEngine;
import org.rythmengine.extension.ICodeType;
import org.rythmengine.extension.ISourceCodeEnhancer;
import org.rythmengine.logger.Logger;
import org.rythmengine.play.RythmPlugin;
import org.rythmengine.template.ITemplate;
import org.rythmengine.utils.F;
import org.rythmengine.utils.JSONWrapper;
import org.rythmengine.utils.S;
import play.jobs.Every;
import play.mvc.Scope;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import static common.Helper.eq;

/**
 * User: freewind
 * Date: 13-3-18
 * Time: 下午6:07
 */
public class Code implements Serializable {

    public String id;
    public String desc;
    public String params;
    public List<CodeFile> files;
    public boolean showInMenu;

    public boolean isNew() {
        return S.empty(id);
    }

    private static final Object lock = new Object();

    private static final Map<String, F.T2<RythmEngine, Long>> engines = new HashMap<String, F.T2<RythmEngine, Long>>();

    private Properties _conf(Properties userConf, String sessId) {
        Properties conf = new Properties();
        if (null != userConf) conf.putAll(userConf);
        conf.put("resource.loader", new InMemoryResourceLoader(sessId));
        conf.put("default.code_type", ICodeType.DefImpl.HTML);
        conf.put("engine.mode", Rythm.Mode.dev);
        conf.put("cache.prod_only.enabled", false);
        RythmEngine playRE = RythmPlugin.engine;
        conf.put("engine.class_loader.parent", playRE.classLoader().getParent());
        conf.put("engine.class_loader.byte_code_helper", playRE.conf().byteCodeHelper());
        conf.put("sandbox.allowed_system_properties", "java.io.tmpdir,file.encoding,user.dir,line.separator,java.vm.name,java.protocol.handler.pkgs,suppressRawWhenUnchecked,tolerateIllegalAmbiguousVarargsInvocation");
        conf.put("engine.playframework.enabled", true);
        conf.put("codegen.source_code_enhancer", new ISourceCodeEnhancer() {
            @Override
            public List<String> imports() {
                List<String> l = new ArrayList<String>();
                l.add("demo.*");
                return l;
            }

            @Override
            public String sourceCode() {
                return null;
            }

            @Override
            public Map<String, ?> getRenderArgDescriptions() {
                return Collections.EMPTY_MAP;
            }

            @Override
            public void setRenderArgs(ITemplate template) {
            }
        });
        conf.put("rythm.i18n.message.resolver", new InMemoryMessageResolver(sessId));
        //conf.put("log.source.java.enabled", false);
        //conf.put("log.source.template.enabled", false);
        return conf;
    }

    private RythmEngine engine(Properties p) {
        final Scope.Session sess = Scope.Session.current();
        final String sessId = sess.getId();
        Properties conf = _conf(p, sessId);
        String userConf = null == p ? "" : p.toString();
        synchronized (lock) {
            F.T2<RythmEngine, Long> t2 = engines.get(sessId + userConf);
            RythmEngine e;
            if (t2 == null) {
                e = new RythmEngine(conf);
                Logger.info(">>>>>>>>>>Cache enabled: %s", e.conf().cacheEnabled());
                e.registerTransformer(Order.class);
            } else {
                e = t2._1;
            }
            engines.put(sessId + userConf, F.T2(e, System.currentTimeMillis()));
            return e;
        }
    }

    public String render() throws IOException {
        CodeFile main = getMainCodeFile();
        // FIXME? Is it correct to use a file? (which will make rythm use FileTemplateResource)
        Map<String, Object> context = new HashMap();
        context.put("session-id", Scope.Session.current().getId());
        JSONWrapper json = S.empty(params) ? null : JSONWrapper.wrap(params);
        Map<String, Object> map = null == json ? null : json.getObject();
        Properties p = null;
        if (null != map && map.containsKey("_conf")) {
            JSONObject o = (JSONObject)map.get("_conf");
            p = new Properties();
            for (String k : o.keySet()) {
                p.put(k, o.get(k));
            }
        }
        RythmEngine rythm = engine(p);
        if (S.notEmpty(params)) {
            return rythm.sandbox(context).render(main.getKey(), json);
        } else {
            return rythm.sandbox(context).render(main.getKey());
        }
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public CodeFile getMainCodeFile() {
        for (CodeFile file : files) {
            if (file.isMain) {
                return file;
            }
        }
        return files.get(0);
    }

    public CodeFile findCodeFile(String path) {
        // find by fullname first
        for (CodeFile file : files) {
            if (eq(file.filename, path)) {
                return file;
            }
        }
        // find by prefix
        for (CodeFile file : files) {
            if (file.filename.startsWith(path + ".")) {
                return file;
            }
        }
        return null;
    }

    public void save(String sessionId) {
        for (CodeFile file : files) {
            file.save(sessionId);
        }
        InMemoryMessageResolver.save(this, sessionId);
    }
    
    @Every("10mn")
    public static class SessionCleaner extends play.jobs.Job {
        @Override
        public void doJob() throws Exception {
            List<String> toBeRemoved = new ArrayList<String>();
            for (String sessId : engines.keySet()) {
                F.T2<RythmEngine, Long> t2 = engines.get(sessId);
                if ((t2._2 + 1000 * 60 * 5) < System.currentTimeMillis()) toBeRemoved.add(sessId);
            }
            for (String sessId : toBeRemoved) {
                engines.remove(sessId);
            }
        }
    }
    
}

