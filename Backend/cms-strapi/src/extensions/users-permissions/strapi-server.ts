/** End-user identity belongs exclusively to Spring/BFF (issue #99).
 * Keep CMS admin routes and the public editorial permission strategy.
 * Content API tokens use Strapi's separate api-token strategy.
 */
export default (plugin: any) => {
  plugin.routes['content-api'].routes = [];
  plugin.services.jwt = () => ({
    getToken(ctx: any) {
      // A supplied credential must never become an anonymous/public fallback.
      if (ctx.request?.header?.authorization) {
        throw new Error('End-user authentication is disabled in the CMS');
      }
      return null;
    },
    issue() {
      throw new Error('End-user authentication is disabled in the CMS');
    },
    verify() {
      throw new Error('End-user authentication is disabled in the CMS');
    },
  });
  return plugin;
};
