#展示对象组装器

很多视图对象（VO）展示时，需要关联其他的对象一起展示，当视图是VO List时，很多开发者都会在for循环查询关联对象，从而引起性能问题
好的做法是，收集VO List的所有要关联对象的外键ID List，然后查询数据库后获取数据List，再分别组装给每个VO对象
此工具类用于简化上述步骤，只需开发者提供ID List查询数据方法即可快速完成List VO对象的组装

~~~
  Order User 1:1
  Order Item 1:N
  1-1 => 查询Order 要以OrderVO + UserVO 形式展示
  1-1 list => 查询OrderList 要以(OrderVO + UserVO)List 形式展示
  1-N => 查询Order 要以OrderVO + ItemVOList 形式展示
  1-N list => 查询Order 要以(OrderVO + ItemVOList)List 形式展示
~~~