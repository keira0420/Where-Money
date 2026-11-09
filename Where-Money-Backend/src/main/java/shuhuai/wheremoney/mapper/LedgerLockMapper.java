package shuhuai.wheremoney.mapper;

import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface LedgerLockMapper {
    @Select("SELECT id FROM book WHERE id=#{id} FOR UPDATE")
    Integer book(Integer id);
    @Select("<script>SELECT id FROM asset WHERE id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> ORDER BY id FOR UPDATE</script>")
    List<Integer> assets(@Param("ids") List<Integer> ids);
}
