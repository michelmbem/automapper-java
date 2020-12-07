# AutoMapper
A Java implementation of the [AutoMapper](https://automapper.org/) utility package.

## Features:

* Automatically maps java entities of two different types.
* Can also map arrays and collections of entities.
* Allow to control how the mapping is applied (ignore, convert properties or map them from non-similarly named properties).
* Uses a builtin introspection tool for properties discovery.

## Limitations:

* By the way Java handles generic types, I couldn't figure a way to determine what is the actual type of items in a generic collection. Though, AutoMapper is not able to automatically convert collection's items to the target type. Users should always manually configure that in their profile (which is as simple as appending a *.forMember("collection_member", mapTo(DesiredType.class)) to the invocation of createMap)*.
* AutoMapper uses introspection which makes it slower than [MapStruct](https://mapstruct.org/) for example.

## Usage:

1. First clone this repository

    ``git clone https://github.com/michelmbem/automapper-java.git``

2. Then open a command prompt into the repository's folder and build it

    ``mvn clean install``

3. Add a reference to AutoMapper to your project's POM file

    ````
    <dependency>
        <groupId>org.addy</groupId>
        <artifactId>automapper</artifactId>
        <version>1.0.0</version>
    </dependency>
    ````

    **N.B.:** AutoMapper will be loaded from your local Maven repository.

4. Create a custom profile for your project

    ````
    package ...

    import org.addy.automapper.Profile;
    import ...

    public class MyProfile extends Profile {
        
        public MyProfile() {
            createMap(MyEntity.class, MyDTO.class);
            createMap(MyDTO.class, MyEntity.class)
                .forMember("id", ignore());

            createMap(OtherEntity.class, OtherDTO.class);
                .forMember("myEntityCollection", mapTo(MyDTO.class));
            createMap(OtherDTO.class, OtherEntity.class);
                .forMember("id", ignore())
                .forMember("myDTOCollection", mapTo(MyEntity.class));
            
            ...
        }

    }
    ````

5. Register AutoMapper as a bean

    ````
    package ...

    import javax.enterprise.inject.Produces;
    import org.addy.automapper.AutoMapper;

    public class EJBResources {
        
        @Produces
        public AutoMapper produceAutoMapper() {
            return new AutoMapper(new MyProfile());
        }

        ...

    }

    ````

    **N.B.:** the above example assumes you are using CDI. In a Spring-based application you should replace the *@Produce* annotation by a *@Bean* annotation.

6. Start injecting AutoMapper in your beans

    ````
    package ...

    import java.util.Optional;
    import javax.inject.Inject;
    import org.addy.automapper.AutoMapper;
    import ...

    public class MyServiceImpl implements MyService {

        private final MyDao dao;
        private final AutoMapper mapper;
        
        @Inject
        public MyServiceImpl(MyDao dao, AutoMapper mapper) {
            this.dao = dao;
            this.mapper = mapper;
        }

        @Override
        public List<MyDTO> findAll() {
            return mapper.map(dao.findAll(), MyDTO.class);
        }

        @Override
        public Optional<MyDTO> findById(Integer id) {
            MyEntity entity = dao.findById(id);
            return Optional.ofNullable(mapper.map(entity, MyDTO.class));
        }

        @Override
        public void save(MyDTO dto) {
            MyEntity entity = mapper.map(dto, MyEntity.class);
            dao.save(entity);
            mapper.map(entity, dto); // Transfer generated values to the DTO
        }

        @Override
        public void update(Integer id, MyDTO dto) {
            MyEntity entity = dto.findById(id);
            // Handle this in the web layer and return a 404 response
            if (entity == null) throw new EntityNotFoundException(MyEntity.class, id);
            mapper.map(dto, entity); // Properties copy
            entity = dao.update(entity);
            mapper.map(entity, dto); // Transfer generated values to the DTO
        }

        @Override
        public void delete(Integer id) {
            MyEntity entity = dto.findById(id);
            // Handle this in the web layer and return a 404 response
            if (entity == null) throw new EntityNotFoundException(MyEntity.class, id);
            dao.delete(entity);
        }

        ...

    }


    ````

That's all!!