package kim.hanjie.common.compose;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 组装数据工具类
 *
 * 很多视图对象（VO）展示时，需要关联其他的对象一起展示，当视图是VO List时，很多开发者都会在for循环查询关联对象，从而引起性能问题
 * 好的做法是，收集VO List的所有要关联对象的外键ID List，然后查询数据库后获取数据List，再分别组装给每个VO对象
 * 此工具类用于简化上述步骤，只需开发者提供ID List查询数据方法即可快速完成List VO对象的组装
 *
 * Order User 1:1
 * Order Item 1:N
 *
 * 1-1 => 查询Order 要以OrderVO + UserVO 形式展示
 * 1-1 list => 查询OrderList 要以(OrderVO + UserVO)List 形式展示
 * 1-N => 查询Order 要以OrderVO + ItemVOList 形式展示
 * 1-N list => 查询Order 要以(OrderVO + ItemVOList)List 形式展示
 *
 * @author han
 * @date 2021/3/1
 */
public class ComposeUtils {


    /**
     * 组装fk（外键）对象
     * 1-1
     *
     * @param d D对象，内有一个外键属性，即SD的主键
     * @param fkFunction 获取D的外键，即SD的主键
     * @param sdFunction 通过SD获取对应的对象
     * @param sdSetFunction 处理SD对象方法，如setSD
     * @param <D> 对象
     * @param <FK> D的一个属性，D需要关联对象SD的主键
     * @param <SD> 需要关联的对象
     * @return D
     */
    public static <D, FK, SD> D subData(D d,
                                        Function<D, FK> fkFunction,
                                        Function<FK, SD> sdFunction,
                                        BiConsumer<D, SD> sdSetFunction) {
        if (d == null || fkFunction == null || sdFunction == null || sdSetFunction == null) {
            return d;
        }
        FK fk = fkFunction.apply(d);
        if (fk == null) {
            return d;
        }
        SD apply = sdFunction.apply(fk);
        if (apply == null) {
            return d;
        }
        sdSetFunction.accept(d, apply);
        return d;
    }

    /**
     * 组装fk（外键）对象并转换
     * 1-1
     * <pre>
     * 场景：如果有Order订单，其中有一个属性是userId，代表是属于哪个User的订单
     *
     * Order:
     *     oid: 订单ID
     *     userId: 订单属于哪个用户
     * User:
     *     userId：用户ID
     *     userName：用户名字
     *
     * 页面展示VO为：
     * OrderVO
     *     oid: 订单ID
     *     userId: 订单属于哪个用户
     *     userName: 用户名字
     * fkFunction:   Order::getUserId
     * sdFunction: 根据userId查询User
     * dConvertFunction: Order -> OrderVo
     * sdFunction: OrderVO设置User
     * </pre>
     *
     * @param d D对象，内有一个外键属性，即SD的主键
     * @param fkFunction 获取D的外键，即SD的主键
     * @param sdFunction 通过SD获取对应的对象
     * @param dConvertFunction d的转换器
     * @param sdSetFunction D转换后对象处理SD对象方法，如setSD
     * @param <D> 对象
     * @param <FK> D的一个属性，D需要关联对象SD的主键
     * @param <SD> 需要关联的对象
     * @param <V> 转换后的对象
     * @return V D转换后的对象，且设置了SD对象
     */
    public static <D, FK, SD, V> V subData(D d,
                                           Function<D, FK> fkFunction,
                                           Function<FK, SD> sdFunction,
                                           Function<D, V> dConvertFunction,
                                           BiConsumer<V, SD> sdSetFunction) {
        if (d == null || fkFunction == null || sdFunction == null || dConvertFunction == null || sdSetFunction == null) {
            return null;
        }
        FK fk = fkFunction.apply(d);
        V v = dConvertFunction.apply(d);
        if (fk == null) {
            return v;
        }
        SD apply = sdFunction.apply(fk);
        if (apply == null) {
            return v;
        }
        sdSetFunction.accept(v, apply);
        return v;
    }

    /**
     * 组装fk（外键）对象
     * 1-N
     *
     * @param d D对象，内有一个外键属性，即SD的主键
     * @param pkFunction 获取D的外键，即SD的主键
     * @param sdListFunction 通过SD获取对应的对象list
     * @param sdListSetFunction SD对象list方法，如setSD
     * @param <D> 对象
     * @param <PK> D的一个属性，D需要关联对象SD的主键
     * @param <SD> 需要关联的对象
     * @return D
     */
    public static <D, PK, SD> D subDataList(D d,
                                            Function<D, PK> pkFunction,
                                            Function<PK, List<SD>> sdListFunction,
                                            BiConsumer<D, List<SD>> sdListSetFunction) {
        if (d == null || pkFunction == null || sdListFunction == null || sdListSetFunction == null) {
            return d;
        }
        PK pk = pkFunction.apply(d);
        if (pk == null) {
            return d;
        }
        List<SD> apply = sdListFunction.apply(pk);
        if (apply == null) {
            return d;
        }
        sdListSetFunction.accept(d, apply);
        return d;
    }


    /**
     * 1-N 组装
     *
     * <pre>
     * 场景：如果有Order订单，有ItemId的list
     *
     * Order:
     *     oid: 订单ID
     *     List<itemId>: 订单包含的商品
     * Item:
     *     itemId：商品ID
     *     itemName：商品名字
     *     oid: 订单ID
     *
     * 页面展示VO为：
     * OrderVO
     *     oid: 订单ID
     *     userId: 订单属于哪个用户
     *     userName: 用户名字
     *     List<ItemVO>: 商品列表
     *
     * pkFunction:   Order::getOid
     * sdListFunction: oid -> itemIdList -> ItemList -> ItemVOList
     * dConvertFunction: Order -> OrderVo
     * sdListSetFunction: OrderVO设置ItemVOList
     * </pre>
     *
     * @param d D对象，内有一个主键属性，SD中关联D的属性
     * @param pkFunction 获取D的主键，SD中关联D的属性
     * @param sdListFunction 通过PK获取对应的对象List
     * @param dConvertFunction d的转换器
     * @param sdListSetFunction D转换后对象处理SD对象方法，如setSD
     * @param <D> 对象
     * @param <PK> D的一个属性（一般为主键），SD中关联D的属性
     * @param <SD> 需要关联的对象
     * @param <V> 转换后的对象
     * @return V D转换后的对象，且设置了SD对象
     */
    public static <D, PK, SD, V> V subDataList(D d,
                                               Function<D, PK> pkFunction,
                                               Function<PK, List<SD>> sdListFunction,
                                               Function<D, V> dConvertFunction,
                                               BiConsumer<V, List<SD>> sdListSetFunction) {
        if (d == null || pkFunction == null || sdListFunction == null || dConvertFunction == null || sdListSetFunction == null) {
            return null;
        }
        PK pk = pkFunction.apply(d);
        V v = dConvertFunction.apply(d);
        if (pk == null) {
            return v;
        }
        List<SD> apply = sdListFunction.apply(pk);
        if (apply == null) {
            return v;
        }
        sdListSetFunction.accept(v, apply);
        return v;
    }

    /**
     * 1-1 以list形式
     *
     * @param list 需要转换的list
     * @param idFunction list中每个元素外键id
     * @param dataFunction id list 转换成 id -> data的map，通过ids去查相关数据库获取dataList，并转为map
     * @param consumer d的设置data方法
     */
    public static <D, ID, SD> List<D> listSubData(List<D> list,
                                                  Function<D, ID> idFunction,
                                                  Function<List<ID>, Map<ID, SD>> dataFunction,
                                                  BiConsumer<D, SD> consumer) {
        if (list == null || list.isEmpty() || idFunction == null || dataFunction == null || consumer == null) {
            return list;
        }
        Set<ID> ids = list.stream().map(idFunction).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return list;
        }
        Map<ID, SD> allData = dataFunction.apply(new ArrayList<>(ids));
        for (D d : list) {
            ID id = idFunction.apply(d);
            if (id != null) {
                SD data = allData.get(id);
                if (data != null) {
                    consumer.accept(d, data);
                }
            }
        }
        return list;
    }

    /**
     * 1-1 以list形式
     *
     * @param list 需要转换的list
     * @param idFunction list中每个元素外键id
     * @param dataFunction id list 转换成 id -> data的map，通过ids去查相关数据库获取dataList，并转为map
     * @param convertFunction t转换为v t的数据转换
     * @param consumer v的设置data方法
     */
    public static <D, ID, SD, V> List<V> listSubData(List<D> list,
                                                     Function<D, ID> idFunction,
                                                     Function<List<ID>, Map<ID, SD>> dataFunction,
                                                     Function<D, V> convertFunction,
                                                     BiConsumer<V, SD> consumer) {
        if (list == null || list.isEmpty() || idFunction == null || dataFunction == null || convertFunction == null || consumer == null) {
            return new ArrayList<>();
        }
        Set<ID> ids = list.stream().map(idFunction).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return list.stream().map(convertFunction).collect(Collectors.toList());
        }
        Map<ID, SD> allData = dataFunction.apply(new ArrayList<>(ids));
        ArrayList<V> vList = new ArrayList<>();
        for (D d : list) {
            V v = convertFunction.apply(d);
            ID id = idFunction.apply(d);
            if (id != null) {
                SD data = allData.get(id);
                if (data != null) {
                    consumer.accept(v, data);
                }
            }
            vList.add(v);
        }
        return vList;
    }

    /**
     * 1-N 以list转换
     *
     * @param list 需要转换的list
     * @param idFunction list中每个元素id
     * @param sidFunction List<ID> 所有ID， 返回ID List<SID>
     * @param dataFunction List<SID> 所有SID， 返回所有 DATA 以MAP形式
     * @param consumer v的设置data方法
     */
    public static <D, ID, SD, SID> List<D> listSubDataList(List<D> list,
                                                           Function<D, ID> idFunction,
                                                           Function<List<ID>, Map<ID, List<SID>>> sidFunction,
                                                           Function<List<SID>, Map<SID, SD>> dataFunction,
                                                           BiConsumer<D, List<SD>> consumer) {
        if (list == null || list.isEmpty() || idFunction == null || dataFunction == null || consumer == null) {
            return list;
        }
        Set<ID> ids = list.stream().map(idFunction).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return list;
        }
        Map<ID, List<SID>> id2sidMap = sidFunction.apply(new ArrayList<>(ids));
        if (id2sidMap == null || id2sidMap.isEmpty()) {
            return list;
        }
        Collection<List<SID>> values = id2sidMap.values();
        Set<SID> sidSet = new HashSet<>();
        values.forEach(sidSet::addAll);
        Map<SID, SD> allData = dataFunction.apply(new ArrayList<>(sidSet));
        if (allData == null || allData.isEmpty()) {
            return list;
        }
        for (D d : list) {
            List<SID> sidList = id2sidMap.get(idFunction.apply(d));
            if (sidList != null) {
                List<SD> dataList = new ArrayList<>();
                for (SID sid : sidList) {
                    SD data = allData.get(sid);
                    if (data != null) {
                        dataList.add(data);
                    }
                }
                consumer.accept(d, dataList);
            }
        }
        return list;
    }


    /**
     * 1-N 以list转换
     *
     * @param list 需要转换的list
     * @param idFunction list中每个元素id
     * @param sidFunction List<ID> 所有ID， 返回ID List<SID>
     * @param dataFunction List<SID> 所有SID， 返回所有 DATA 以MAP形式
     * @param convertFunction t转换为v t的数据转换
     * @param consumer v的设置data方法
     */
    public static <D, ID, SD, SID, V> List<V> listSubDataList(List<D> list,
                                                              Function<D, ID> idFunction,
                                                              Function<List<ID>, Map<ID, List<SID>>> sidFunction,
                                                              Function<List<SID>, Map<SID, SD>> dataFunction,
                                                              Function<D, V> convertFunction,
                                                              BiConsumer<V, List<SD>> consumer) {
        if (list == null || list.isEmpty() || idFunction == null || dataFunction == null || convertFunction == null || consumer == null) {
            return new ArrayList<>();
        }
        Set<ID> ids = list.stream().map(idFunction).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return list.stream().map(convertFunction).collect(Collectors.toList());
        }
        Map<ID, List<SID>> id2sidMap = sidFunction.apply(new ArrayList<>(ids));
        if (id2sidMap == null || id2sidMap.isEmpty()) {
            return list.stream().map(convertFunction).collect(Collectors.toList());
        }
        Collection<List<SID>> values = id2sidMap.values();
        Set<SID> sidSet = new HashSet<>();
        values.forEach(sidSet::addAll);
        Map<SID, SD> allData = dataFunction.apply(new ArrayList<>(sidSet));
        if (allData == null || allData.isEmpty()) {
            return list.stream().map(convertFunction).collect(Collectors.toList());
        }
        ArrayList<V> vList = new ArrayList<>();
        for (D d : list) {
            V v = convertFunction.apply(d);
            List<SID> sidList = id2sidMap.get(idFunction.apply(d));
            if (sidList != null) {
                List<SD> dataList = new ArrayList<>();
                for (SID sid : sidList) {
                    SD data = allData.get(sid);
                    if (data != null) {
                        dataList.add(data);
                    }
                }
                consumer.accept(v, dataList);
            }
            vList.add(v);
        }
        return vList;
    }
}

