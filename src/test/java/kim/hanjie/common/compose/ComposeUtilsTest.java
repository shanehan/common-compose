package kim.hanjie.common.compose;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ComposeUtilsTest {

    @BeforeEach
    void setUp() {
        initData();
    }


    private List<Order> orderList;
    private List<User> userList;
    private List<Item> itemList;
    private List<OrderItem> orderItemList;


    private void initData() {
        orderList = new ArrayList<>();
        orderList.add(Order.builder().oid(1L).orderName("1号订单").userId(1L).money(200L).build());
        orderList.add(Order.builder().oid(2L).orderName("2号订单").userId(2L).money(300L).build());
        orderList.add(Order.builder().oid(3L).orderName("3号订单").userId(3L).money(500L).build());
        orderList.add(Order.builder().oid(4L).orderName("4号订单").userId(1L).money(800L).build());

        userList = new ArrayList<>();
        userList.add(User.builder().uid(1L).name("zhangsan").build());
        userList.add(User.builder().uid(2L).name("lisi").build());
        userList.add(User.builder().uid(3L).name("wangwu").build());
        userList.add(User.builder().uid(4L).name("zhaoliu").build());

        itemList = new ArrayList<>();
        itemList.add(Item.builder().itemId(11L).name("item_11").build());
        itemList.add(Item.builder().itemId(22L).name("item_22").build());
        itemList.add(Item.builder().itemId(33L).name("item_33").build());
        itemList.add(Item.builder().itemId(44L).name("item_44").build());


        orderItemList = new ArrayList<>();
        orderItemList.add(OrderItem.builder().itemId(11L).orderId(1L).build());
        orderItemList.add(OrderItem.builder().itemId(22L).orderId(1L).build());
        orderItemList.add(OrderItem.builder().itemId(11L).orderId(2L).build());
        orderItemList.add(OrderItem.builder().itemId(11L).orderId(3L).build());
        orderItemList.add(OrderItem.builder().itemId(22L).orderId(3L).build());
        orderItemList.add(OrderItem.builder().itemId(33L).orderId(3L).build());
        orderItemList.add(OrderItem.builder().itemId(22L).orderId(4L).build());
    }

    @Test
    void subData() {
        initData();
        Order orderById = findOrderById(1L);
        OrderVO vo = ComposeUtils.subData(orderById, Order::getUserId, this::findUserById, this::orderDo2VO, (v, sd) -> v.setUserName(sd.getName()));
        //
        assertOrder(orderById, "zhangsan", vo);
    }

    @Test
    void subDataList() {
        initData();
        Order d = findOrderById(3L);
        OrderVO orderVO = ComposeUtils.subDataList(d, Order::getOid, (oid -> {
            List<OrderItem> orderItems = findOrderItemByOid(oid);
            List<Long> itemIds = orderItems.stream().map(OrderItem::getItemId).collect(Collectors.toList());
            return findItems(itemIds);
        }), this::orderDo2VO, OrderVO::setItemList);
        assertOrder(d, null, orderVO);
        assertEquals(orderVO.getItemList().size(), 3);
        assertEquals(orderVO.getItemList().get(0).getName(), "item_11");
        assertEquals(orderVO.getItemList().get(1).getName(), "item_22");
        assertEquals(orderVO.getItemList().get(2).getName(), "item_33");
    }

    private void assertOrder(Order expected, String userName, OrderVO actual) {
        assertEquals(expected.getOid(), actual.getOid());
        assertEquals(expected.getOrderName(), actual.getOrderName());
        assertEquals(expected.getUserId(), actual.getUserId());
        assertEquals(expected.getMoney(), actual.getMoney());
        assertEquals(userName, actual.getUserName());
    }


    private User findUserById(Long userId) {
        return userList.stream().filter(user -> user.getUid().equals(userId)).findFirst().orElse(null);
    }

    private Order findOrderById(Long oid) {
        return orderList.stream().filter(order -> order.getOid().equals(oid)).findFirst().orElse(null);
    }

    private List<OrderItem> findOrderItemByOid(Long oid) {
        return orderItemList.stream().filter(oi -> oi.getOrderId().equals(oid)).collect(Collectors.toList());
    }

    private List<Item> findItems(List<Long> itemIds) {
        return itemList.stream().filter(i -> itemIds.contains(i.getItemId())).collect(Collectors.toList());
    }

    private OrderVO orderDo2VO(Order order) {
        OrderVO orderVO = new OrderVO();
        orderVO.setOid(order.getOid());
        orderVO.setOrderName(order.getOrderName());
        orderVO.setUserId(order.getUserId());
        orderVO.setMoney(order.getMoney());
        return orderVO;
    }

    @Data
    @Builder
    private static class Order {
        private Long oid;
        private String orderName;
        private Long userId;
        private Long money;
    }

    @Data
    @Builder
    private static class User {
        private Long uid;
        private String name;
    }

    @Data
    @Builder
    private static class Item {
        private Long itemId;
        private String name;
    }


    @Data
    @Builder
    private static class OrderItem {
        private Long orderId;
        private Long itemId;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    private static class OrderVO {
        private Long oid;
        private String orderName;
        private Long userId;
        private String userName;
        private Long money;
        private List<Item> itemList;
    }
}