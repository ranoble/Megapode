=============
Data Handling
=============

Megapode uses DbUtils for it's persistance handling. Each actor has it's own connection,
and they operate in isolation. Transaction handling is up to you. Remember what Stan Lee 
said...

Unlike and ORM, you *will* need to define and build all your database tables.

Like tasks and calculations, Data Accessors need custom interfaces to interact via their proxy. 

TODO: rename

Once again, you need to annotate the Accessor class with PerstanceAccessor, implement IDataAccesor or extend PersistanceBase.

You will need to define your methods and a matching interface. Remember that
you should always return a future. 

The data accessor will be provided with the database connection. 

Example update and data extraction that maps to the data you want to extract ::

    // First define a bean: Comment.java
    public class Comment {
        Integer id;
        String comment;

        public Integer getId() {
            return id;
        }
        public void setCommentId(Integer id) {
            this.id = id;
        }
        public String getComment() {
            return comment;
        }
	public void setComment(String comment) {
            this.comment = comment;
        }
    }

    // The interface
    public interface ICommentAccessor {
        Future<Integer> insert(Comment comment);
        Future<List<Comment>> getComments();
    }

    // The implementation

    @PersistanceAccessor
    public class CommentAccessor extends PersistanceBase implements
        IDataAccessor, IUserProfileData {

        public UserProfileData(Map<Layers, ActorRef> routers,
            ActorRef coordinatingActor, UntypedActorContext actorContext,
            Connection connection) {
            super(routers, coordinatingActor, actorContext, connection);
        }
        
        @Override
        public Future<Integer> insert(Comment comment){
            //TODO: Implement 
        }
        
        @Override
        public Future<List<Comment>> getComments(){
            QueryRunner run = new QueryRunner();
            ResultSetHandler<List<User>> handler = new BeanListHandler(Comment.class);
            try {
                List<Comment> results = run.query(this.connection, 
                    "select * from comment, handler);
                return Futures.successful(results);
            } catch (SQLException e) {
                return Futures.failed(e);
            }
        }
    }  

The callable then is implemented as such ::

    ICommentAccessor commentAccess = DataAccessors.get(ICommentAccessor.class,     CommentAccessor.class, this);
    Future<List<Comment>> comments = commentAccess.getComments();

